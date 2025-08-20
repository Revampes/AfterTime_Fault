package com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import java.util.List;
import java.awt.Color;

public class FastHotKeyGui extends GuiScreen {
    // Default radii (will be overridden by config each frame)
    private static final int DEFAULT_OUTER_RADIUS = 150;
    private static final int DEFAULT_INNER_RADIUS = 40;
    // New UI constants
    private static final float GAP_PIXELS = 5f;
    private static final float ARROW_BASE_HALFWIDTH = 10f;
    private static final float ARROW_LENGTH = 20f;
    private static final float ARROW_MARGIN = 3f;
    // Label layout constants
    private static final float LABEL_RADIUS_FACTOR = 0.7f;     // Where labels sit between inner/outer
    private static final float LABEL_SIDE_MARGIN_PX = 4f;      // Keep a small angular margin
    private static final float LABEL_VERTICAL_MARGIN_PX = 3f;  // Keep a small radial margin
    private static final float LABEL_MAX_SCALE = 3.5f;         // Avoid absurdly large text

    // Outline
    private static final float OUTLINE_THICKNESS = 8f;         // Outline stroke thickness

    private int centerX;
    private int centerY;
    private int regionCount = 0;
    // Track last mouse position while the GUI is open
    private int lastMouseX = 0;
    private int lastMouseY = 0;

    @Override
    public void initGui() {
        super.initGui();
        this.centerX = width / 2;
        this.centerY = height / 2;
        List<FastHotkeyEntry> entries = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES;
        this.regionCount = Math.min(12, entries.size());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Update last known mouse position
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        // Center
        centerX = width / 2;
        centerY = height / 2;

        // Load configurable radii and colors
        int innerRadius = getSafeInnerRadius();
        int outerRadius = getSafeOuterRadius(innerRadius);
        int proxRange = getOutlineProxRange();
        Color innerNear = getColorOrDefault("fhk_inner_near_color", new Color(255,255,255,255));
        Color innerFar  = getColorOrDefault("fhk_inner_far_color",  new Color(0,0,0,255));
        Color outerNear = getColorOrDefault("fhk_outer_near_color", new Color(255,255,255,255));
        Color outerFar  = getColorOrDefault("fhk_outer_far_color",  new Color(0,0,0,255));

        // No commands configured
        if (regionCount == 0) {
            String msg = "No Fast Hotkey commands configured";
            String hint = "Open Mod Settings > Fast Hotkey > Settings to add up to maximum 12 commands";
            drawCenteredString(fontRendererObj, msg, centerX, centerY - 10, 0xFFFFFF);
            drawCenteredString(fontRendererObj, hint, centerX, centerY + 5, 0xAAAAAA);
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        // Draw ring sectors with constant pixel gaps
        double sectorSize = 2 * Math.PI / regionCount;
        for (int i = 0; i < regionCount; i++) {
            double baseStart = i * sectorSize;
            double baseEnd = (i + 1) * sectorSize;
            drawRingSectorWithPixelGap(centerX, centerY, innerRadius, outerRadius, baseStart, baseEnd, GAP_PIXELS, 0x80000000);
        }

        // New: Angular gradient outlines (near->far colors), with gaps preserved per sector
        drawAngularGradientRingOutlineWithGaps(centerX, centerY, innerRadius, regionCount, GAP_PIXELS, mouseX, mouseY, proxRange, innerNear, innerFar);
        drawAngularGradientRingOutlineWithGaps(centerX, centerY, outerRadius, regionCount, GAP_PIXELS, mouseX, mouseY, proxRange, outerNear, outerFar);

        // Temporarily disabled old hover effect (white arcs on hovered region)
        // ...existing code for old hover arcs removed...

        // Labels (scaled to fit their wedge)
        List<FastHotkeyEntry> entries = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES;
        for (int i = 0; i < regionCount; i++) {
            double midAngle = Math.PI * 2 * i / regionCount + Math.PI / regionCount;
            double rLabel = (innerRadius + (outerRadius - innerRadius) * LABEL_RADIUS_FACTOR);
            int x = (int)(centerX + Math.cos(midAngle) * rLabel);
            int y = (int)(centerY + Math.sin(midAngle) * rLabel);
            String label = entries.get(i).label;
            if (label == null || label.trim().isEmpty()) label = "Command " + (i + 1);
            drawScaledCenteredLabel(label, x, y, rLabel, sectorSize, 0xFFFFFF, innerRadius, outerRadius);
        }

        // Direction arrow following mouse, positioned near inner radius
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double mouseAngle = Math.atan2(dy, dx);
        double arrowRadius = innerRadius + (ARROW_LENGTH * 0.5f) + ARROW_MARGIN;
        drawArrowAtAngle(centerX, centerY, mouseAngle, arrowRadius, ARROW_BASE_HALFWIDTH, ARROW_LENGTH, 0xFFFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // Public API for confirming selection when hotkey is released
    public void onHotkeyReleased() {
        int innerRadius = getSafeInnerRadius();
        int outerRadius = getSafeOuterRadius(innerRadius);
        int region = getHoveredRegion(this.lastMouseX, this.lastMouseY, innerRadius, outerRadius);
        if (region != -1) {
            List<FastHotkeyEntry> entries = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES;
            if (region < entries.size()) {
                String cmd = entries.get(region).command;
                if (cmd != null && !cmd.trim().isEmpty()) {
                    Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
                }
            }
        }
        mc.displayGuiScreen(null);
    }

    // Compute and draw the label at the largest scale that fits its wedge and ring thickness
    private void drawScaledCenteredLabel(String text, int x, int y, double rLabel, double sectorSize, int color, int innerRadius, int outerRadius) {
        if (text == null || text.isEmpty()) return;

        int baseWidth = fontRendererObj.getStringWidth(text);
        int baseHeight = fontRendererObj.FONT_HEIGHT;
        if (baseWidth <= 0 || baseHeight <= 0) return;

        // Available angular width at label radius (account for an approximate gap and side margins)
        double gapAngleAtR = GAP_PIXELS / Math.max(1.0, rLabel);
        double sideMarginAngle = LABEL_SIDE_MARGIN_PX / Math.max(1.0, rLabel);
        double usableAngle = Math.max(0.0, sectorSize - gapAngleAtR - 2.0 * sideMarginAngle);
        double chordWidth = 2.0 * rLabel * Math.sin(Math.max(0.0, usableAngle / 2.0));

        // Width-constrained scale (keep a tiny safety margin)
        double allowedWidth = Math.max(0.0, chordWidth - 2.0);
        float widthScale = (float) (allowedWidth / baseWidth);

        // Height-constrained scale based on ring thickness around rLabel
        double radialMax = Math.min(rLabel - innerRadius, outerRadius - rLabel) - LABEL_VERTICAL_MARGIN_PX;
        radialMax = Math.max(0.0, radialMax);
        float heightScale = (float) ((2.0 * radialMax) / baseHeight);

        float scale = Math.min(widthScale, heightScale);
        scale = Math.min(scale, LABEL_MAX_SCALE);
        if (!(scale > 0f)) return; // nothing fits

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1.0f);
        drawCenteredString(fontRendererObj, text, 0, -(fontRendererObj.FONT_HEIGHT / 2), color);
        GlStateManager.popMatrix();
    }

    // Draw arc between explicit angles
    private void drawCircleArcAngles(int x, int y, int radius, double startAngle, double endAngle, int color, float thickness) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GL11.glColor4f(r, g, b, a);
        GL11.glLineWidth(thickness);

        int steps = 64;
        double step = Math.max((endAngle - startAngle) / steps, 1e-4);
        GL11.glBegin(GL11.GL_LINE_STRIP);
        for (double angle = startAngle; angle <= endAngle + 1e-6; angle += step) {
            GL11.glVertex2f((float)(x + Math.cos(angle) * radius), (float)(y + Math.sin(angle) * radius));
        }
        GL11.glEnd();

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    // Draw a ring sector with constant pixel gap at inner and outer edges
    private void drawRingSectorWithPixelGap(int x, int y, int innerRadius, int outerRadius, double baseStart, double baseEnd, float gapPx, int color) {
        if (baseEnd <= baseStart) return;
        double innerTrim = gapPx / (2.0 * Math.max(1.0, innerRadius));
        double outerTrim = gapPx / (2.0 * Math.max(1.0, outerRadius));
        double innerStart = baseStart + innerTrim;
        double innerEnd = baseEnd - innerTrim;
        double outerStart = baseStart + outerTrim;
        double outerEnd = baseEnd - outerTrim;
        if (innerEnd <= innerStart || outerEnd <= outerStart) return;

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        GL11.glColor4f(r, g, b, a);

        int steps = 48;
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        for (int i = 0; i <= steps; i++) {
            double t = (double)i / steps;
            double oa = outerStart + t * (outerEnd - outerStart);
            double ia = innerStart + t * (innerEnd - innerStart);
            float ox = (float)(x + Math.cos(oa) * outerRadius);
            float oy = (float)(y + Math.sin(oa) * outerRadius);
            float ix = (float)(x + Math.cos(ia) * innerRadius);
            float iy = (float)(y + Math.sin(ia) * innerRadius);
            GL11.glVertex2f(ox, oy);
            GL11.glVertex2f(ix, iy);
        }
        GL11.glEnd();

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    // Small triangle arrow at fixed radius pointing to a given angle
    private void drawArrowAtAngle(int cx, int cy, double angle, double radius, float baseHalfWidth, float length, int color) {
        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        float dx = (float)Math.cos(angle);
        float dy = (float)Math.sin(angle);
        float px = -dy;
        float py = dx;

        float tipX = (float)(cx + dx * (radius + length * 0.5f));
        float tipY = (float)(cy + dy * (radius + length * 0.5f));
        float baseCenterX = (float)(cx + dx * (radius - length * 0.5f));
        float baseCenterY = (float)(cy + dy * (radius - length * 0.5f));
        float baseLx = baseCenterX + px * baseHalfWidth;
        float baseLy = baseCenterY + py * baseHalfWidth;
        float baseRx = baseCenterX - px * baseHalfWidth;
        float baseRy = baseCenterY - py * baseHalfWidth;

        boolean scissorWasEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();
        if (scissorWasEnabled) GL11.glDisable(GL11.GL_SCISSOR_TEST);

        // Fill
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(tipX, tipY);
        GL11.glVertex2f(baseLx, baseLy);
        GL11.glVertex2f(baseRx, baseRy);
        GL11.glEnd();

        // Outline
        GL11.glLineWidth(2.0f);
        GL11.glColor4f(0f, 0f, 0f, 0.85f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(tipX, tipY);
        GL11.glVertex2f(baseLx, baseLy);
        GL11.glVertex2f(baseRx, baseRy);
        GL11.glEnd();

        if (scissorWasEnabled) GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private int getHoveredRegion(int mouseX, int mouseY, int innerRadius, int outerRadius) {
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > outerRadius || distance < innerRadius || regionCount == 0) return -1;

        double angle = Math.atan2(dy, dx);
        if (angle < 0) angle += 2 * Math.PI;

        double sectorSize = 2 * Math.PI / regionCount;
        int idx = (int)(angle / sectorSize);
        double local = angle - idx * sectorSize;
        double gapAngle = GAP_PIXELS / Math.max(1.0, distance);
        if (local < gapAngle / 2.0 || local > sectorSize - gapAngle / 2.0) {
            return -1; // pointer is in the gap region
        }
        return idx;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Close only on Escape; hotkey release is handled externally
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            int innerRadius = getSafeInnerRadius();
            int outerRadius = getSafeOuterRadius(innerRadius);
            int region = getHoveredRegion(mouseX, mouseY, innerRadius, outerRadius);
            if (region != -1) {
                List<FastHotkeyEntry> entries = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES;
                if (region < entries.size()) {
                    String cmd = entries.get(region).command;
                    if (cmd != null && !cmd.trim().isEmpty()) {
                        Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
                    }
                }
                mc.displayGuiScreen(null);
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // =============================
    // Helpers for config + outlines
    // =============================
    private int getSafeInnerRadius() {
        Object v = AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.get("fhk_inner_radius").Data;
        int r = (v instanceof Integer) ? (Integer) v : DEFAULT_INNER_RADIUS;
        return Math.max(10, Math.min(400, r));
    }
    private int getSafeOuterRadius(int innerRadius) {
        Object v = AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.get("fhk_outer_radius").Data;
        int r = (v instanceof Integer) ? (Integer) v : DEFAULT_OUTER_RADIUS;
        return Math.max(innerRadius + 10, Math.min(600, r));
    }
    private int getOutlineProxRange() {
        Object v = AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.get("fhk_outline_prox_range").Data;
        int r = (v instanceof Integer) ? (Integer) v : 120;
        return Math.max(10, Math.min(2000, r));
    }
    private Color getColorOrDefault(String key, Color def) {
        Object v = AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.get(key).Data;
        return (v instanceof Color) ? (Color) v : def;
    }

    // New: Angular gradient outline with gaps preserved
    private void drawAngularGradientRingOutlineWithGaps(int cx, int cy, int radius, int regionCount, float gapPx, int mouseX, int mouseY, int proxRange, Color near, Color far) {
        if (regionCount <= 0 || radius <= 0) return;
        double mouseAngle = Math.atan2(mouseY - cy, mouseX - cx);
        double dist = Math.hypot(mouseX - cx, mouseY - cy);
        float fRad = clamp01(1.0f - (float)(Math.abs(dist - radius) / Math.max(1, proxRange)));
        double sectorSize = (Math.PI * 2.0) / regionCount;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GL11.glLineWidth(OUTLINE_THICKNESS);

        // Precompute color channels
        float nr = near.getRed() / 255f, ng = near.getGreen() / 255f, nb = near.getBlue() / 255f, na = near.getAlpha() / 255f;
        float fr = far.getRed() / 255f,  fg = far.getGreen() / 255f,  fb = far.getBlue() / 255f,  fa = far.getAlpha() / 255f;

        // Per-sector draw with gap trimming at this radius
        double gapAngle = (gapPx / Math.max(1.0, radius));
        int stepsPerFull = 192; // smoothness
        int stepsPerSector = Math.max(8, stepsPerFull / Math.max(1, regionCount));

        for (int i = 0; i < regionCount; i++) {
            double baseStart = i * sectorSize;
            double baseEnd = (i + 1) * sectorSize;
            double start = baseStart + gapAngle * 0.5;
            double end = baseEnd - gapAngle * 0.5;
            if (end <= start) continue;

            GL11.glBegin(GL11.GL_LINE_STRIP);
            for (int s = 0; s <= stepsPerSector; s++) {
                double t = (double)s / (double)stepsPerSector;
                double a = start + t * (end - start);
                // Angular factor: 1 at same angle as mouse, 0 at opposite side
                double dAng = angularDistance(a, mouseAngle);
                float fAng = (float)(1.0 - (dAng / Math.PI));
                fAng = clamp01(fAng);
                // Combine radial and angular closeness for mixing
                float mix = clamp01(fAng * fRad);
                // Lerp colors: far -> near
                float cr = fr + (nr - fr) * mix;
                float cg = fg + (ng - fg) * mix;
                float cb = fb + (nb - fb) * mix;
                float ca = fa + (na - fa) * mix;
                GL11.glColor4f(cr, cg, cb, ca);
                GL11.glVertex2f((float)(cx + Math.cos(a) * radius), (float)(cy + Math.sin(a) * radius));
            }
            GL11.glEnd();
        }

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private static double angularDistance(double a, double b) {
        double d = Math.abs(a - b) % (Math.PI * 2.0);
        return d > Math.PI ? (2.0 * Math.PI - d) : d;
    }
    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }
}
