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
    private static final float OUTLINE_THICKNESS = 10f;         // Outline stroke thickness

    // Background hover animation defaults
    private static final int DEFAULT_BG_INFLUENCE_RADIUS = 30;   // px distance where background hover fades out
    private static final float DEFAULT_BG_MAX_EXTEND = 18f;       // px maximum outward bulge at outer edge

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

        // Background hover animation config
        int bgInfluence = getBgInfluenceRadius();
        float bgMaxExtend = getBgMaxExtend();
        // Defaults now 50% grey (uniform)
        Color bgNear = getColorOrDefault("fhk_bg_near_color", new Color(128, 128, 128, 255));
        Color bgFar  = getColorOrDefault("fhk_bg_far_color",  new Color(128, 128, 128, 255));
        // If user saved very bright colors for both, gently clamp to mid-grey but PRESERVE alpha
        if (isVeryBright(bgNear) && isVeryBright(bgFar)) {
            int preservedA = Math.min(bgNear.getAlpha(), bgFar.getAlpha());
            bgNear = new Color(128, 128, 128, preservedA);
            bgFar  = new Color(128, 128, 128, preservedA);
        }
        // Only ensure gradient direction if mismatched; don't force a gradient when equal
        if (luminance(bgNear) > luminance(bgFar)) {
            Color tmp = bgNear; bgNear = bgFar; bgFar = tmp;
        }

        // No commands configured
        if (regionCount == 0) {
            String msg = "No Fast Hotkey commands configured";
            String hint = "Open Mod Settings > Fast Hotkey > Settings to add up to maximum 12 commands";
            drawCenteredString(fontRendererObj, msg, centerX, centerY - 10, 0xFFFFFF);
            drawCenteredString(fontRendererObj, hint, centerX, centerY + 5, 0xAAAAAA);
            super.drawScreen(mouseX, mouseY, partialTicks);
            return;
        }

        // Determine hovered sector index for extension logic
        int hoveredIndex = getHoveredRegion(mouseX, mouseY, innerRadius, outerRadius);

        // Draw ring sector backgrounds with distance-following alpha and outward extension near the cursor
        drawRingSectorBackgroundWithHover(centerX, centerY, innerRadius, outerRadius, regionCount,
                GAP_PIXELS, mouseX, mouseY, bgInfluence, bgNear, bgFar, bgMaxExtend, hoveredIndex);

        // New: Angular gradient outlines (near->far colors), with gaps preserved per sector
        drawAngularGradientRingOutlineWithGaps(centerX, centerY, innerRadius, regionCount, GAP_PIXELS, mouseX, mouseY, proxRange, innerNear, innerFar);
        // Outer outline now extends outward with animation to match background extension, but only on hovered sector
        drawAngularGradientOuterOutlineWithExtend(centerX, centerY, outerRadius, regionCount, GAP_PIXELS,
                mouseX, mouseY, proxRange, outerNear, outerFar, bgInfluence, bgMaxExtend, hoveredIndex);

        // Temporarily disabled old hover effect (white arcs on hovered region)
        // ...existing code for old hover arcs removed...

        // Labels (scaled to fit their wedge)
        List<FastHotkeyEntry> entries = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES;
        double sectorSize = 2 * Math.PI / regionCount;
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
        Object showArrowCfg = safeCfgGet("fhk_show_arrow");
        boolean showArrow = !(showArrowCfg instanceof Boolean) || ((Boolean) showArrowCfg);
        if (showArrow) {
            double dx = mouseX - centerX;
            double dy = mouseY - centerY;
            double mouseAngle = Math.atan2(dy, dx);
            double arrowRadius = innerRadius + (ARROW_LENGTH * 0.5f) + ARROW_MARGIN;
            drawArrowAtAngle(centerX, centerY, mouseAngle, arrowRadius, ARROW_BASE_HALFWIDTH, ARROW_LENGTH, 0xFFFFFFFF);
        }

        // On-screen quick labels to clarify settings (placeholders like terminal settings)
        int infoX = 8;
        int infoY = 8;
        int cInfo = 0xCCFFFFFF; // semi-white
        fontRendererObj.drawString("Inner radius (px): " + innerRadius, infoX, infoY, cInfo);
        fontRendererObj.drawString("Outer radius (px): " + outerRadius, infoX, infoY + 10, cInfo);
        fontRendererObj.drawString("Outline influence (px): " + proxRange, infoX, infoY + 20, cInfo);
        fontRendererObj.drawString("BG influence (px): " + bgInfluence, infoX, infoY + 30, cInfo);
        fontRendererObj.drawString("BG max extend (px): " + (int)bgMaxExtend, infoX, infoY + 40, cInfo);

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
        Object v = safeCfgGet("fhk_inner_radius");
        int r = (v instanceof Integer) ? (Integer) v : DEFAULT_INNER_RADIUS;
        return Math.max(10, Math.min(400, r));
    }
    private int getSafeOuterRadius(int innerRadius) {
        Object v = safeCfgGet("fhk_outer_radius");
        int r = (v instanceof Integer) ? (Integer) v : DEFAULT_OUTER_RADIUS;
        return Math.max(innerRadius + 10, Math.min(600, r));
    }
    private int getOutlineProxRange() {
        Object v = safeCfgGet("fhk_outline_prox_range");
        int r = (v instanceof Integer) ? (Integer) v : 120;
        return Math.max(10, Math.min(2000, r));
    }
    private Color getColorOrDefault(String key, Color def) {
        Object v = safeCfgGet(key);
        return (v instanceof Color) ? (Color) v : def;
    }
    private Object safeCfgGet(String key) {
        try {
            Object entry = AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.get(key);
            // entry may be null or may not have a Data field accessible in this context
            if (entry == null) return null;
            try {
                // Reflective safety not needed; directly access Data if possible
                return AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.get(key).Data;
            } catch (Throwable t) {
                return null;
            }
        } catch (Throwable t) {
            return null;
        }
    }
    // Background hover config
    private int getBgInfluenceRadius() {
        Object v = safeCfgGet("fhk_bg_influence_radius");
        int r = (v instanceof Integer) ? (Integer) v : DEFAULT_BG_INFLUENCE_RADIUS;
        return Math.max(20, Math.min(3000, r));
    }
    private float getBgMaxExtend() {
        Object v = safeCfgGet("fhk_bg_max_extend");
        float f = (v instanceof Number) ? ((Number) v).floatValue() : DEFAULT_BG_MAX_EXTEND;
        return Math.max(0f, Math.min(60f, f));
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

    // New: Angular gradient outer outline that also extends outward following the background animation
    private void drawAngularGradientOuterOutlineWithExtend(int cx, int cy, int baseRadius, int regionCount, float gapPx,
                                                           int mouseX, int mouseY, int proxRange, Color near, Color far,
                                                           int influenceRadius, float maxExtend, int hoveredIndex) {
        if (regionCount <= 0 || baseRadius <= 0) return;
        double mouseAngle = Math.atan2(mouseY - cy, mouseX - cx);
        double dist = Math.hypot(mouseX - cx, mouseY - cy);
        float fRad = clamp01(1.0f - (float)(Math.abs(dist - baseRadius) / Math.max(1, proxRange)));
        double sectorSize = (Math.PI * 2.0) / regionCount;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GL11.glLineWidth(OUTLINE_THICKNESS);

        float nr = near.getRed() / 255f, ng = near.getGreen() / 255f, nb = near.getBlue() / 255f, na = near.getAlpha() / 255f;
        float fr = far.getRed() / 255f,  fg = far.getGreen() / 255f,  fb = far.getBlue() / 255f,  fa = far.getAlpha() / 255f;

        double gapAngle = (gapPx / Math.max(1.0, baseRadius));
        int stepsPerFull = 192; // revert to previous smoothness
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

                // Per-vertex angular influence relative to cursor angle; extend only hovered sector
                double dAng = angularDistance(a, mouseAngle);
                float angInfluence = clamp01((float)(1.0 - dAng / Math.PI));
                float radCloseness = clamp01(1.0f - (float)(Math.abs(dist - baseRadius) / Math.max(1, influenceRadius)));
                float extend = (i == hoveredIndex) ? (maxExtend * (angInfluence * radCloseness)) : 0f;
                double radius = baseRadius + extend;

                // Color blend relative to cursor angle
                float fAng = clamp01((float)(1.0 - (dAng / Math.PI)));
                float mix = clamp01(fAng * fRad);
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

    // New: background sector fill that follows cursor distance with alpha, and extends outward when close
    private void drawRingSectorBackgroundWithHover(int cx, int cy, int innerRadius, int outerRadius, int regionCount,
                                                   float gapPx, int mouseX, int mouseY, int influenceRadius,
                                                   Color near, Color far, float maxExtend, int hoveredIndex) {
        if (regionCount <= 0 || outerRadius <= innerRadius) return;
        double sectorSize = (Math.PI * 2.0) / regionCount;
        double mouseAngle = Math.atan2(mouseY - cy, mouseX - cx);
        double mouseDist = Math.hypot(mouseX - cx, mouseY - cy);
        boolean centerMode = mouseDist <= innerRadius;

        // Precompute color channels for near/far
        float nr = near.getRed() / 255f, ng = near.getGreen() / 255f, nb = near.getBlue() / 255f, na = near.getAlpha() / 255f;
        float fr = far.getRed() / 255f,  fg = far.getGreen() / 255f,  fb = far.getBlue() / 255f,  fa = far.getAlpha() / 255f;

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableCull();

        // Compute separate trims for inner and outer edges to keep pixel gaps PARALLEL
        double innerTrim = gapPx / (2.0 * Math.max(1.0, innerRadius));
        double outerTrim = gapPx / (2.0 * Math.max(1.0, outerRadius));

        int stepsPerFull = 128; // smoothness
        int stepsPerSector = Math.max(10, stepsPerFull / Math.max(1, regionCount));

        for (int i = 0; i < regionCount; i++) {
            double baseStart = i * sectorSize;
            double baseEnd = (i + 1) * sectorSize;
            double innerStart = baseStart + innerTrim;
            double innerEnd = baseEnd - innerTrim;
            double outerStart = baseStart + outerTrim;
            double outerEnd = baseEnd - outerTrim;
            if (innerEnd <= innerStart || outerEnd <= outerStart) continue;

            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            if (centerMode || colorsEffectivelySame(near, far)) {
                // Uniform color, no extension in center mode; still respect per-edge trims for parallel gaps
                GL11.glColor4f(fr, fg, fb, fa);
                for (int s = 0; s <= stepsPerSector; s++) {
                    double t = (double)s / (double)stepsPerSector;
                    double oa = outerStart + t * (outerEnd - outerStart);
                    double ia = innerStart + t * (innerEnd - innerStart);
                    float ix = (float)(cx + Math.cos(ia) * innerRadius);
                    float iy = (float)(cy + Math.sin(ia) * innerRadius);
                    float ox = (float)(cx + Math.cos(oa) * outerRadius);
                    float oy = (float)(cy + Math.sin(oa) * outerRadius);
                    GL11.glVertex2f(ox, oy);
                    GL11.glVertex2f(ix, iy);
                }
            } else {
                for (int s = 0; s <= stepsPerSector; s++) {
                    double t = (double)s / (double)stepsPerSector;
                    // Base angles before applying dynamic outer trim
                    double baseOuterStart = outerStart;
                    double baseOuterEnd = outerEnd;
                    double baseInnerStart = innerStart;
                    double baseInnerEnd = innerEnd;

                    // Angular positions for inner with fixed trim (inner radius doesn't change)
                    double ia = baseInnerStart + t * (baseInnerEnd - baseInnerStart);

                    // Angular influence for outward extension (1 near the cursor direction, 0 opposite)
                    double mouseAng = mouseAngle;
                    // We'll derive a provisional outer angle to evaluate influence; start with untrimmed interpolation
                    double oaProvisional = baseOuterStart + t * (baseOuterEnd - baseOuterStart);
                    double dAngOuter = angularDistance(oaProvisional, mouseAng);
                    float angInfluence = clamp01((float)(1.0 - dAngOuter / Math.PI));

                    // Compute outward extension based on angular influence and radial closeness to the outer edge
                    float radCloseness = clamp01(1.0f - (float)(Math.abs(mouseDist - outerRadius) / Math.max(1, influenceRadius)));
                    float extend = (i == hoveredIndex) ? (maxExtend * (angInfluence * radCloseness)) : 0f;

                    // Compute dynamic outer trim based on the local effective radius (outerRadius + extend)
                    double localOuterRadius = outerRadius + extend;
                    double dynTrim = gapPx / (2.0 * Math.max(1.0, localOuterRadius));
                    // Ensure trim never collapses the sector
                    double maxTrim = (baseOuterEnd - baseOuterStart) * 0.49;
                    dynTrim = Math.min(dynTrim, maxTrim);

                    double oa = (baseOuterStart + dynTrim) + t * ((baseOuterEnd - dynTrim) - (baseOuterStart + dynTrim));

                    // Vertex positions (outer uses its own angle plus dynamic extension)
                    float ix = (float)(cx + Math.cos(ia) * innerRadius);
                    float iy = (float)(cy + Math.sin(ia) * innerRadius);
                    float ox = (float)(cx + Math.cos(oa) * localOuterRadius);
                    float oy = (float)(cy + Math.sin(oa) * localOuterRadius);

                    // Distance-driven color at each vertex (closer to cursor -> closer to 'near' color)
                    float pOuter = clamp01(1.0f - (float)(Math.hypot(mouseX - ox, mouseY - oy) / Math.max(1, influenceRadius)));
                    float pInner = clamp01(1.0f - (float)(Math.hypot(mouseX - ix, mouseY - iy) / Math.max(1, influenceRadius)));

                    // Outer vertex color
                    float oR = fr + (nr - fr) * pOuter;
                    float oG = fg + (ng - fg) * pOuter;
                    float oB = fb + (nb - fb) * pOuter;
                    float oA = fa + (na - fa) * pOuter;
                    GL11.glColor4f(oR, oG, oB, oA);
                    GL11.glVertex2f(ox, oy);

                    // Inner vertex color
                    float iR = fr + (nr - fr) * pInner;
                    float iG = fg + (ng - fg) * pInner;
                    float iB = fb + (nb - fb) * pInner;
                    float iA = fa + (na - fa) * pInner;
                    GL11.glColor4f(iR, iG, iB, iA);
                    GL11.glVertex2f(ix, iy);
                }
            }
            GL11.glEnd();
        }

        GlStateManager.enableCull();
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

    // Helper: consider two colors effectively the same if RGBA differences are tiny
    private boolean colorsEffectivelySame(Color a, Color b) {
        if (a == null || b == null) return false;
        int dr = Math.abs(a.getRed()   - b.getRed());
        int dg = Math.abs(a.getGreen() - b.getGreen());
        int db = Math.abs(a.getBlue()  - b.getBlue());
        int da = Math.abs(a.getAlpha() - b.getAlpha());
        return dr < 8 && dg < 8 && db < 8 && da < 8;
    }

    private boolean isVeryBright(Color c) {
        if (c == null) return false;
        return luminance(c) >= 200f; // threshold for near-white/very light grey
    }

    private float luminance(Color c) {
        if (c == null) return 0f;
        return (0.299f * c.getRed() + 0.587f * c.getGreen() + 0.114f * c.getBlue());
    }
}
