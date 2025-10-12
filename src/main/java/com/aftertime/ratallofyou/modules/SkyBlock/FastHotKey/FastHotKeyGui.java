package com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey;

import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry;
import com.aftertime.ratallofyou.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import java.util.List;
import java.awt.Color;

import com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap;

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

    // Edit mode (add/delete inside the radial GUI)
    private boolean editMode = false;

    // New: Preset side panel state (edit mode only)
    private GuiTextField presetCreateField;
    private boolean presetCreateFieldInit = false;
    private int keyCapturePresetIndex = -1; // -1 = none; else waiting for key for this preset
    private int selectedPresetIndex = -1; // mirrors store active index

    // Inline editor state
    private boolean editingEntry = false;
    private int editingIndex = -1;
    private GuiTextField labelField;
    private GuiTextField commandField;
    private String originalLabel = "";
    private String originalCommand = "";

    public static void open() {
        Minecraft.getMinecraft().displayGuiScreen(new FastHotKeyGui());
    }

    @Override
    public void initGui() {
        super.initGui();
        this.centerX = width / 2;
        this.centerY = height / 2;
        // Use store-backed entries
        List<FastHotkeyEntry> entries = FastHotkeyStore.getInstance().getActiveEntries();
        this.regionCount = Math.min(12, entries.size());
        // Initialize preset selection mirror
        this.selectedPresetIndex = FastHotkeyStore.getInstance().getActiveIndex();
        if (editMode && editingEntry) {
            createOrLayoutTextFields();
        }
        // Initialize create field bounds
        presetCreateFieldInit = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Update last known mouse position
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;

        // Center
        centerX = width / 2;
        centerY = height / 2;

        // Keep regionCount in sync with entries size (supports add/delete live)
        List<FastHotkeyEntry> entriesForCount = FastHotkeyStore.getInstance().getActiveEntries();
        this.regionCount = Math.min(12, entriesForCount == null ? 0 : entriesForCount.size());

        // Draw left preset panel when in edit mode (behind radial or as overlay)
        if (editMode) {
            drawPresetPanel(mouseX, mouseY);
        }

        // Load configurable radii and colors
        int innerRadius = getSafeInnerRadius();
        int outerRadius = getSafeOuterRadius(innerRadius);
        int proxRange = getOutlineProxRange();
        Color innerNear = argbToColor(ModConfig.fhkInnerNearColor, new Color(255,255,255,255));
        Color innerFar  = argbToColor(ModConfig.fhkInnerFarColor,  new Color(0,0,0,255));
        Color outerNear = argbToColor(ModConfig.fhkOuterNearColor, new Color(255,255,255,255));
        Color outerFar  = argbToColor(ModConfig.fhkOuterFarColor,  new Color(0,0,0,255));

        // Background hover animation config
        int bgInfluence = getBgInfluenceRadius();
        float bgMaxExtend = getBgMaxExtend();
        // Defaults now 50% grey (uniform)
        Color bgNear = argbToColor(ModConfig.fhkBgNearColor, new Color(128, 128, 128, 255));
        Color bgFar  = argbToColor(ModConfig.fhkBgFarColor,  new Color(128, 128, 128, 255));
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
            String hint = editMode ? "Press A to add a new command" : "Press E to enter edit mode";
            drawCenteredString(fontRendererObj, msg, centerX, centerY - 10, 0xFFFFFF);
            drawCenteredString(fontRendererObj, hint, centerX, centerY + 5, 0xAAAAAA);
            // Edit HUD
            drawEditHud();
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

        // Labels (scaled to fit their wedge)
        List<FastHotkeyEntry> entries = FastHotkeyStore.getInstance().getActiveEntries();
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
        boolean showArrow = ModConfig.fhkShowArrow;
        if (showArrow) {
            double dx = mouseX - centerX;
            double dy = mouseY - centerY;
            double mouseAngle = Math.atan2(dy, dx);
            double arrowRadius = innerRadius + (ARROW_LENGTH * 0.5f) + ARROW_MARGIN;
            drawArrowAtAngle(centerX, centerY, mouseAngle, arrowRadius, ARROW_BASE_HALFWIDTH, ARROW_LENGTH, 0xFFFFFFFF);
        }

        // Edit HUD
        drawEditHud();

        // Inline editor overlay when editing a slot
        if (editMode && editingEntry) {
            int panelW = 260;
            int panelH = 70;
            int px = centerX - panelW / 2;
            int py = height - panelH - 20;
            drawRect(px - 4, py - 4, px + panelW + 4, py + panelH + 4, 0xAA000000);

            int titleColor = 0xFFFFFF;
            fontRendererObj.drawString("Editing slot " + (editingIndex + 1), px, py - 12, titleColor);
            fontRendererObj.drawString("Label:", px, py + 4, 0xCCCCCC);
            fontRendererObj.drawString("Command:", px, py + 36, 0xCCCCCC);

            createOrLayoutTextFields();
            labelField.updateCursorCounter();
            commandField.updateCursorCounter();
            labelField.drawTextBox();
            commandField.drawTextBox();

            String hint = "Enter: next/save  Tab: switch  Ctrl+S: save  Esc: cancel";
            fontRendererObj.drawString(hint, px, py + panelH + 8, 0xAAAAAA);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // Draw the left-hand preset management panel (edit mode only)
    private void drawPresetPanel(int mouseX, int mouseY) {
        final int panelX = 8;
        final int panelY = 8;
        final int panelW = 260;
        final int panelH = height - 16;
        // Panel background and border
        drawRect(panelX, panelY, panelX + panelW, panelY + panelH, 0xAA111111);
        drawRect(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, 0xFF000000);
        fontRendererObj.drawString("Presets", panelX + 6, panelY + 6, 0xFFFFFF);

        int y = panelY + 22;
        // Create new preset input and button
        if (presetCreateField == null) {
            presetCreateField = new GuiTextField(100, this.fontRendererObj, panelX + 6, y, panelW - 6 - 66, 16);
            presetCreateField.setMaxStringLength(64);
        } else if (!presetCreateFieldInit) {
            presetCreateField.xPosition = panelX + 6;
            presetCreateField.yPosition = y;
            presetCreateField.width = panelW - 6 - 66;
            presetCreateField.height = 16;
            presetCreateFieldInit = true;
        }
        if (presetCreateField != null) presetCreateField.updateCursorCounter();
        presetCreateField.drawTextBox();
        int btnX = panelX + panelW - 60;
        int btnY = y;
        drawRect(btnX, btnY, btnX + 54, btnY + 16, 0xFF2E7D32);
        drawCenteredString(fontRendererObj, "Create", btnX + 27, btnY + 3, 0xFFFFFFFF);
        y += 22;

        // Existing presets list
        List<com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset> presets = FastHotkeyStore.getInstance().getPresetsView();
        int rowH = 16;
        int gap = 4;
        for (int i = 0; i < presets.size(); i++) {
            com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset p = presets.get(i);
            int rowY = y;
            // Row background highlight if selected
            int bg = (i == selectedPresetIndex) ? 0x333388FF : 0x22000000;
            drawRect(panelX + 2, rowY - 1, panelX + panelW - 2, rowY + rowH + 1, bg);

            // Toggle box 14x14
            int tSize = 14; int toggleX = panelX + 6; int toggleY = rowY + (rowH - tSize) / 2;
            int toggleColor = p.enabled ? 0xFF2E7D32 : 0xFFC62828;
            drawRect(toggleX, toggleY, toggleX + tSize, toggleY + tSize, toggleColor);

            // Remove button (rightmost)
            int rmW = 54; int rmX = panelX + panelW - rmW - 2; int rmY = rowY;
            drawRect(rmX, rmY, rmX + rmW, rmY + rowH, 0xFFC62828);
            drawCenteredString(fontRendererObj, "Remove", rmX + rmW / 2, rmY + 3, 0xFFFFFFFF);

            // Keybind box 80px placed left of remove with 6px gap
            int keyW = 80; int keyX = rmX - 6 - keyW; int keyY = rowY;
            drawRect(keyX, keyY, keyX + keyW, keyY + rowH, 0xFF222222);
            String keyLabel;
            if (keyCapturePresetIndex == i) keyLabel = "Press a key...";
            else keyLabel = (p.keyCode <= 0) ? "Unbound" : Keyboard.getKeyName(p.keyCode);
            if (keyLabel == null || keyLabel.trim().isEmpty()) keyLabel = "Unknown";
            fontRendererObj.drawString(keyLabel, keyX + 4, keyY + 4, 0xFFFFFFFF);

            // Name area between toggle and key
            int nameX = toggleX + tSize + 6; int nameW = Math.max(40, keyX - 6 - nameX);
            String nm = p.name + (i == FastHotkeyStore.getInstance().getActiveIndex() ? "  (Active)" : "");
            fontRendererObj.drawString(nm, nameX, rowY + 4, 0xFFFFFFFF);

            y += rowH + gap;
        }
    }

    // Small edit-mode HUD and hints (bottom-left)
    private void drawEditHud() {
        int hudX = 6;
        int hudY = height - 40;
        int col = 0xCCFFFFFF;
        String mode = editMode ? "Edit Mode (E to exit)" : "Press E to edit";
        fontRendererObj.drawString(mode, hudX, hudY, col);
        if (editMode) {
            if (editingEntry) {
                fontRendererObj.drawString("Enter/Tab/Ctrl+S/Esc affect editor", hudX, hudY + 10, col);
                fontRendererObj.drawString("Editing: clicks won't execute/delete", hudX, hudY + 20, col);
            } else {
                fontRendererObj.drawString("LMB: Edit hovered  A/Ins: Add  RMB/Del: Delete hovered", hudX, hudY + 10, col);
                fontRendererObj.drawString("ESC: Close   Clicks won't execute", hudX, hudY + 20, col);
            }
        }
    }

    // Public API for confirming selection when hotkey is released
    public void onHotkeyReleased() {
        // Ignore selection when editing; keep GUI open
        if (editMode) return;
        int innerRadius = getSafeInnerRadius();
        int outerRadius = getSafeOuterRadius(innerRadius);
        int region = getHoveredRegion(this.lastMouseX, this.lastMouseY, innerRadius, outerRadius);
        if (region != -1) {
            List<FastHotkeyEntry> entries = FastHotkeyStore.getInstance().getActiveEntries();
            if (region < entries.size()) {
                String cmd = entries.get(region).command;
                if (cmd != null && !cmd.trim().isEmpty()) {
                    boolean handled = false;
                    try {
                        if (HotbarSwap.INSTANCE != null) handled = HotbarSwap.INSTANCE.tryTriggerLocal(cmd);
                    } catch (Throwable ignored) {}
                    if (!handled) {
                        Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
                    }
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
        // Handle preset key capture first when in edit mode
        if (editMode && keyCapturePresetIndex >= 0) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                // Clear binding and cancel capture
                FastHotkeyStore.getInstance().setPresetKey(keyCapturePresetIndex, 0);
                FastHotkeyStore.getInstance().setPresetEnabled(keyCapturePresetIndex, false);
                syncAllConfigFromStore();
                keyCapturePresetIndex = -1; return;
            }
            if (keyCode > 0) {
                // Ignore NONE
                String name = Keyboard.getKeyName(keyCode);
                if (name != null && !name.trim().isEmpty() && !"NONE".equalsIgnoreCase(name)) {
                    if (!isKeyDuplicate(keyCode, keyCapturePresetIndex)) {
                        FastHotkeyStore.getInstance().setPresetKey(keyCapturePresetIndex, keyCode);
                        FastHotkeyStore.getInstance().setPresetEnabled(keyCapturePresetIndex, true);
                        // Select this preset as active for editing
                        FastHotkeyStore.getInstance().setActiveIndex(keyCapturePresetIndex);
                        selectedPresetIndex = keyCapturePresetIndex;
                        syncAllConfigFromStore();
                        keyCapturePresetIndex = -1; return;
                    } else {
                        // Duplicate: keep capturing
                        return;
                    }
                } else {
                    // Keep capturing until valid key
                    return;
                }
            }
        }

        // When editing: route typing to Create field if focused (letters, backspace, etc.)
        if (editMode && presetCreateField != null && presetCreateField.isFocused()) {
            // ENTER -> create; ESC -> unfocus; otherwise pass to textbox
            if (keyCode == Keyboard.KEY_RETURN) { handleCreatePreset(); return; }
            if (keyCode == Keyboard.KEY_ESCAPE) { presetCreateField.setFocused(false); return; }
            presetCreateField.textboxKeyTyped(typedChar, keyCode);
            return;
        }

        // Toggle edit mode (disabled while actively editing a slot)
        if (!editingEntry && keyCode == Keyboard.KEY_E) {
            editMode = !editMode;
            return;
        }
        if (editMode && editingEntry) {
            if (labelField != null) labelField.textboxKeyTyped(typedChar, keyCode);
            if (commandField != null) commandField.textboxKeyTyped(typedChar, keyCode);

            boolean ctrl = isCtrlKeyDown();
            if (keyCode == Keyboard.KEY_RETURN) {
                if (labelField.isFocused()) {
                    labelField.setFocused(false);
                    commandField.setFocused(true);
                } else {
                    saveEditingEntry();
                }
                return;
            }
            if (keyCode == Keyboard.KEY_TAB) {
                boolean toLabel = !labelField.isFocused();
                labelField.setFocused(toLabel);
                commandField.setFocused(!toLabel);
                return;
            }
            if (ctrl && keyCode == Keyboard.KEY_S) { saveEditingEntry(); return; }
            if (keyCode == Keyboard.KEY_ESCAPE) { cancelEditingEntry(); return; }
            return;
        }
        if (editMode) {
            // Create via Enter when create field is focused (handled above)
            // Add new slot / Delete hovered
            if (keyCode == Keyboard.KEY_A || keyCode == Keyboard.KEY_INSERT) { addNewEntry(); return; }
            // Delete hovered
            if (keyCode == Keyboard.KEY_DELETE) {
                int innerRadius = getSafeInnerRadius();
                int outerRadius = getSafeOuterRadius(innerRadius);
                int idx = getHoveredRegion(this.lastMouseX, this.lastMouseY, innerRadius, outerRadius);
                if (idx != -1) { deleteEntryAt(idx); }
                return;
            }
        }
        // Close only on Escape; hotkey release is handled externally
        if (keyCode == Keyboard.KEY_ESCAPE) { mc.displayGuiScreen(null); return; }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Edit-mode mouse actions
        if (editMode) {
            // First: handle preset panel interactions
            if (handlePresetPanelClick(mouseX, mouseY, mouseButton)) {
                super.mouseClicked(mouseX, mouseY, mouseButton);
                return;
            }

            if (editingEntry) {
                if (labelField != null) labelField.mouseClicked(mouseX, mouseY, mouseButton);
                if (commandField != null) commandField.mouseClicked(mouseX, mouseY, mouseButton);
                super.mouseClicked(mouseX, mouseY, mouseButton);
                return;
            }
            int innerRadius = getSafeInnerRadius();
            int outerRadius = getSafeOuterRadius(innerRadius);
            int region = getHoveredRegion(mouseX, mouseY, innerRadius, outerRadius);
            if (mouseButton == 0) { // left -> start editing hovered
                if (region != -1) { startEditingEntry(region); return; }
            }
            if (mouseButton == 1) { // right -> delete hovered
                if (region != -1) { deleteEntryAt(region); return; }
            }
            // In edit mode we don't propagate clicks to execute commands
            super.mouseClicked(mouseX, mouseY, mouseButton);
            return;
        }
        // Normal mode: left-click executes and closes
        if (mouseButton == 0) {
            int innerRadius = getSafeInnerRadius();
            int outerRadius = getSafeOuterRadius(innerRadius);
            int region = getHoveredRegion(mouseX, mouseY, innerRadius, outerRadius);
            if (region != -1) {
                List<FastHotkeyEntry> entries = FastHotkeyStore.getInstance().getActiveEntries();
                if (region < entries.size()) {
                    String cmd = entries.get(region).command;
                    if (cmd != null && !cmd.trim().isEmpty()) {
                        boolean handled = false;
                        try {
                            if (HotbarSwap.INSTANCE != null) handled = HotbarSwap.INSTANCE.tryTriggerLocal(cmd);
                        } catch (Throwable ignored) {}
                        if (!handled) {
                            Minecraft.getMinecraft().thePlayer.sendChatMessage(cmd);
                        }
                    }
                }
                mc.displayGuiScreen(null);
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    // Handle clicks within the preset panel; returns true if consumed
    private boolean handlePresetPanelClick(int mouseX, int mouseY, int mouseButton) {
        final int panelX = 8;
        final int panelY = 8;
        final int panelW = 260;
        final int panelH = height - 16;
        boolean inPanel = mouseX >= panelX && mouseX <= panelX + panelW && mouseY >= panelY && mouseY <= panelY + panelH;
        if (!inPanel) return false;

        int y = panelY + 22;
        // Create field
        if (presetCreateField != null) {
            if (mouseY >= y && mouseY <= y + 16) {
                if (mouseX >= presetCreateField.xPosition && mouseX <= presetCreateField.xPosition + presetCreateField.width) {
                    presetCreateField.setFocused(true);
                    keyCapturePresetIndex = -1;
                    return true;
                }
                int btnX = panelX + panelW - 60; int btnY = y;
                if (mouseX >= btnX && mouseX <= btnX + 54 && mouseY >= btnY && mouseY <= btnY + 16) {
                    handleCreatePreset();
                    return true;
                }
            }
            presetCreateField.setFocused(false);
        }
        y += 22;

        // Preset rows
        List<com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset> presets = FastHotkeyStore.getInstance().getPresetsView();
        int rowH = 16; int gap = 4;
        for (int i = 0; i < presets.size(); i++) {
            com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset p = presets.get(i);
            int rowY = y;
            int tSize = 14; int toggleX = panelX + 6; int toggleY = rowY + (rowH - tSize) / 2;
            int rmW = 54; int rmX = panelX + panelW - rmW - 2; int rmY = rowY;
            int keyW = 80; int keyX = rmX - 6 - keyW; int keyY = rowY;
            int nameX = toggleX + tSize + 6; int nameW = Math.max(40, keyX - 6 - nameX);

            boolean overToggle = mouseX >= toggleX && mouseX <= toggleX + tSize && mouseY >= toggleY && mouseY <= toggleY + tSize;
            boolean overName = mouseX >= nameX && mouseX <= nameX + nameW && mouseY >= rowY && mouseY <= rowY + rowH;
            boolean overKey = mouseX >= keyX && mouseX <= keyX + keyW && mouseY >= keyY && mouseY <= keyY + rowH;
            boolean overRemove = mouseX >= rmX && mouseX <= rmX + rmW && mouseY >= rmY && mouseY <= rmY + rowH;

            if (overToggle) {
                if (!p.enabled) {
                    if (p.keyCode <= 0) { keyCapturePresetIndex = i; return true; }
                    if (isKeyDuplicate(p.keyCode, i)) { return true; }
                    FastHotkeyStore.getInstance().setPresetEnabled(i, true);
                } else {
                    FastHotkeyStore.getInstance().setPresetEnabled(i, false);
                }
                syncAllConfigFromStore();
                return true;
            }
            if (overName) {
                FastHotkeyStore.getInstance().setActiveIndex(i);
                selectedPresetIndex = i;
                syncAllConfigFromStore();
                return true;
            }
            if (overKey) { keyCapturePresetIndex = i; return true; }
            if (overRemove) {
                FastHotkeyStore.getInstance().removePreset(i);
                selectedPresetIndex = FastHotkeyStore.getInstance().getActiveIndex();
                syncAllConfigFromStore();
                return true;
            }

            y += rowH + gap;
        }
        return true;
    }

    private void handleCreatePreset() {
        String name = presetCreateField != null ? presetCreateField.getText() : null;
        if (name == null) name = "";
        name = name.trim();
        if (name.isEmpty()) return;
        int idx = FastHotkeyStore.getInstance().addPreset(name);
        selectedPresetIndex = idx;
        if (presetCreateField != null) { presetCreateField.setText(""); presetCreateField.setFocused(false); }
        syncAllConfigFromStore();
    }

    private boolean isKeyDuplicate(int keyCode, int exceptIndex) {
        if (keyCode <= 0) return false;
        List<com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset> list = FastHotkeyStore.getInstance().getPresetsView();
        for (int i = 0; i < list.size(); i++) {
            if (i == exceptIndex) continue;
            com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset p = list.get(i);
            if (p.keyCode == keyCode) return true;
        }
        return false;
    }

    private void syncAllConfigFromStore() {
        // Keep AllConfig live structures in sync so hotkey triggers work immediately
        try {
            com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.FHK_PRESETS = new java.util.ArrayList<>(FastHotkeyStore.getInstance().getPresetsView());
            int active = FastHotkeyStore.getInstance().getActiveIndex();
            com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.setActiveFhkPreset(active);
        } catch (Throwable ignored) {}
    }

    // =============================
    // Inline editor helpers
    // =============================
    private void startEditingEntry(int idx) {
        List<FastHotkeyEntry> list = FastHotkeyStore.getInstance().getActiveEntries();
        if (idx < 0 || idx >= list.size()) return;
        editingEntry = true;
        editingIndex = idx;
        originalLabel = safeString(list.get(idx).label);
        originalCommand = safeString(list.get(idx).command);
        createOrLayoutTextFields();
        if (labelField != null) { labelField.setText(originalLabel); labelField.setFocused(true); }
        if (commandField != null) { commandField.setText(originalCommand); commandField.setFocused(false); }
    }

    private void createOrLayoutTextFields() {
        int panelW = 260;
        int px = centerX - panelW / 2;
        int labelY = height - 70;
        int cmdY = labelY + 32;
        if (labelField == null) {
            labelField = new GuiTextField(1, this.fontRendererObj, px + 50, labelY, panelW - 54, 18);
            labelField.setMaxStringLength(128);
        } else {
            labelField.xPosition = px + 50;
            labelField.yPosition = labelY;
            labelField.width = panelW - 54;
            labelField.height = 18;
        }
        if (commandField == null) {
            commandField = new GuiTextField(2, this.fontRendererObj, px + 50, cmdY, panelW - 54, 18);
            commandField.setMaxStringLength(256);
        } else {
            commandField.xPosition = px + 50;
            commandField.yPosition = cmdY;
            commandField.width = panelW - 54;
            commandField.height = 18;
        }
    }

    private void saveEditingEntry() {
        List<FastHotkeyEntry> list = FastHotkeyStore.getInstance().getActiveEntries();
        if (editingIndex < 0 || editingIndex >= list.size()) { cancelEditingEntry(); return; }
        String newLabel = labelField != null ? labelField.getText() : originalLabel;
        String newCommand = commandField != null ? commandField.getText() : originalCommand;
        List<FastHotkeyEntry> rebuilt = new java.util.ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            FastHotkeyEntry src = list.get(i);
            if (i == editingIndex) {
                rebuilt.add(new FastHotkeyEntry(safeString(newLabel), safeString(newCommand), i));
            } else {
                rebuilt.add(new FastHotkeyEntry(src.label, src.command, i));
            }
        }
        replacePresetEntries(rebuilt);
        cancelEditingEntry();
    }

    private void cancelEditingEntry() {
        editingEntry = false;
        editingIndex = -1;
        labelField = null;
        commandField = null;
        originalLabel = "";
        originalCommand = "";
    }

    private String safeString(String s) { return s == null ? "" : s; }

    // =============================
    // Helpers for config + outlines
    // =============================
    private int getSafeInnerRadius() {
        int r = ModConfig.fhkInnerRadius;
        return Math.max(10, Math.min(400, r));
    }
    private int getSafeOuterRadius(int innerRadius) {
        int r = ModConfig.fhkOuterRadius;
        return Math.max(innerRadius + 10, Math.min(600, r));
    }
    private int getOutlineProxRange() {
        int r = ModConfig.fhkOutlineProxRange;
        return Math.max(10, Math.min(2000, r));
    }
    private int getBgInfluenceRadius() {
        int r = ModConfig.fhkBgInfluenceRadius;
        return Math.max(20, Math.min(3000, r));
    }
    private float getBgMaxExtend() {
        float f = (float) ModConfig.fhkBgMaxExtend;
        return Math.max(0f, Math.min(60f, f));
    }
    private Color argbToColor(int argb, Color def) {
        try { return new Color(argb, true); } catch (Throwable t) { return def; }
    }

    // =============================
    // Editing helpers (add/delete) and persistence
    // =============================
    private void addNewEntry() {
        List<FastHotkeyEntry> list = FastHotkeyStore.getInstance().getActiveEntries();
        if (list.size() >= 12) return;
        int idx = list.size();
        List<FastHotkeyEntry> rebuilt = new java.util.ArrayList<>(list);
        rebuilt.add(new FastHotkeyEntry("", "", idx));
        replacePresetEntries(rebuilt);
    }

    private void deleteEntryAt(int idx) {
        if (editingEntry && idx >= 0) {
            if (idx == editingIndex) {
                cancelEditingEntry();
            } else if (idx < editingIndex) {
                editingIndex -= 1;
            }
        }
        List<FastHotkeyEntry> list = FastHotkeyStore.getInstance().getActiveEntries();
        if (idx < 0 || idx >= list.size()) return;
        List<FastHotkeyEntry> rebuilt = new java.util.ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            if (i == idx) continue;
            FastHotkeyEntry src = list.get(i);
            rebuilt.add(new FastHotkeyEntry(src.label, src.command, rebuilt.size()));
        }
        replacePresetEntries(rebuilt);
    }

    private void replacePresetEntries(List<FastHotkeyEntry> newEntries) {
        // Replace entries in the active preset via the store and persist
        FastHotkeyStore.getInstance().replaceActiveEntries(newEntries);
        this.regionCount = Math.min(12, FastHotkeyStore.getInstance().getActiveEntries().size());
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
                // Uniform color case: still emit BOTH outer and inner vertices to form triangles
                GL11.glColor4f(fr, fg, fb, fa);
                for (int s = 0; s <= stepsPerSector; s++) {
                    double t = (double)s / (double)stepsPerSector;
                    // Use fixed trimmed angles; no dynamic extension in this branch
                    double ia = innerStart + t * (innerEnd - innerStart);
                    double oa = outerStart + t * (outerEnd - outerStart);
                    float ox = (float)(cx + Math.cos(oa) * outerRadius);
                    float oy = (float)(cy + Math.sin(oa) * outerRadius);
                    float ix = (float)(cx + Math.cos(ia) * innerRadius);
                    float iy = (float)(cy + Math.sin(ia) * innerRadius);
                    // Emit outer then inner for proper strip ordering
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
