package com.aftertime.ratallofyou.UI.config.panels;

import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the inline UI for the "Hotbar Swap" module so ModSettingsGui stays simpler.
 * Responsibilities:
 * - Render row-based preset editor (label/message/command + remove)
 * - Handle clicks (focus inputs, add/remove preset)
 * - Handle typing and persist via HotbarSwap module API
 * - Report its own inline height so parent can layout generic options below
 */
public class HotbarSwapPanel {

    // Simple text field implementation local to this panel (decoupled from ModSettingsGui)
    private static class SimpleTextField {
        String text; int x, y, w, h; boolean isEditing = false; long cursorBlinkMs = 0; boolean cursorVisible = false; int cursorPos = 0; int maxLen = 64;
        SimpleTextField(String t) { this.text = t == null ? "" : t; this.cursorPos = this.text.length(); }
        void setBounds(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }
        void draw(int mx, int my, FontRenderer font) {
            Gui.drawRect(x, y, x + w, y + h, Colors.INPUT_BG);
            font.drawStringWithShadow(text, x + 3, y + 4, Colors.INPUT_FG);
            if (isEditing) {
                cursorBlinkMs += 10; if (cursorBlinkMs >= 500) { cursorBlinkMs = 0; cursorVisible = !cursorVisible; }
                if (cursorVisible) {
                    int cx = x + 3 + font.getStringWidth(text.substring(0, Math.min(cursorPos, text.length())));
                    Gui.drawRect(cx, y + 3, cx + 1, y + h - 3, Colors.INPUT_FG);
                }
            }
        }
        boolean isMouseOver(int mx, int my) { return mx >= x && mx <= x + w && my >= y && my <= y + h; }
        void beginEditing(int mx) {
            isEditing = true; cursorBlinkMs = 0; cursorVisible = true;
            int rel = Math.max(0, mx - x); int pos = 0; String s = text; FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
            while (pos < s.length()) { int cw = font.getCharWidth(s.charAt(pos)); if (rel < cw / 2) break; rel -= cw; pos++; }
            cursorPos = pos;
        }
        void handleKeyTyped(char c, int key) {
            if (!isEditing) return;
            switch (key) {
                case org.lwjgl.input.Keyboard.KEY_RETURN: isEditing = false; return;
                case org.lwjgl.input.Keyboard.KEY_BACK: if (cursorPos > 0 && !text.isEmpty()) { text = text.substring(0, cursorPos - 1) + text.substring(cursorPos); cursorPos--; } break;
                case org.lwjgl.input.Keyboard.KEY_LEFT: cursorPos = Math.max(0, cursorPos - 1); break;
                case org.lwjgl.input.Keyboard.KEY_RIGHT: cursorPos = Math.min(text.length(), cursorPos + 1); break;
                default:
                    if (c >= 32 && c != 127) { if (text.length() >= maxLen) return; text = text.substring(0, cursorPos) + c + text.substring(cursorPos); cursorPos++; }
            }
            cursorBlinkMs = 0; cursorVisible = true;
        }
    }

    private static class HotbarPresetRow {
        final int index; final SimpleTextField labelInput; final SimpleTextField triggerInput; final SimpleTextField commandInput;
        int removeBtnX, removeBtnY, removeBtnW, removeBtnH;
        HotbarPresetRow(int idx, String label, String trigger, String command) {
            this.index = idx;
            this.labelInput = new SimpleTextField(label);
            this.triggerInput = new SimpleTextField(trigger);
            this.commandInput = new SimpleTextField(command);
        }
    }

    private final List<HotbarPresetRow> hotbarRows = new ArrayList<>();

    public void rebuildRows() {
        hotbarRows.clear();
        java.util.List<com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.Hotbar> view = com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE != null
                ? com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE.getPresetsView()
                : java.util.Collections.<com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.Hotbar>emptyList();
        for (int i = 0; i < view.size(); i++) {
            com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.Hotbar p = view.get(i);
            hotbarRows.add(new HotbarPresetRow(i, safe(p.name), safe(p.message), safe(p.command)));
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    // Draw the inline content, returning the Y position after drawing this section (to continue with generic options)
    public int drawInline(int mouseX, int mouseY, int contentX, int startY, int contentW, FontRenderer font) {
        int y = startY;
        for (int i = 0; i < hotbarRows.size(); i++) {
            HotbarPresetRow row = hotbarRows.get(i);
            font.drawStringWithShadow("Preset Label:", contentX, y, Colors.COMMAND_TEXT);
            row.labelInput.setBounds(contentX, y + 12, contentW, 16);
            row.labelInput.draw(mouseX, mouseY, font);
            y += 12 + 16 + 6;

            font.drawStringWithShadow("Trigger message:", contentX, y, Colors.COMMAND_TEXT);
            row.triggerInput.setBounds(contentX, y + 12, contentW, 16);
            row.triggerInput.draw(mouseX, mouseY, font);
            y += 12 + 16 + 6;

            font.drawStringWithShadow("Trigger command:", contentX, y, Colors.COMMAND_TEXT);
            row.commandInput.setBounds(contentX, y + 12, contentW, 16);
            row.commandInput.draw(mouseX, mouseY, font);
            y += 12 + 16 + 6;

            int rmW = 70, rmH = 16, rmX = contentX, rmY = y;
            Gui.drawRect(rmX, rmY, rmX + rmW, rmY + rmH, Colors.BUTTON_RED);
            drawCenteredString(font, "Remove", rmX + rmW / 2, rmY + 4, Colors.BUTTON_TEXT);
            row.removeBtnX = rmX; row.removeBtnY = rmY; row.removeBtnW = rmW; row.removeBtnH = rmH;
            y += rmH + 8;

            Gui.drawRect(contentX, y, contentX + contentW, y + 1, 0x22000000);
            y += 6;
        }
        int addW = 90, addH = 16, addX = contentX, addY = y;
        Gui.drawRect(addX, addY, addX + addW, addY + addH, Colors.BUTTON_GREEN);
        drawCenteredString(font, hotbarRows.isEmpty() ? "Add Preset" : "Add Another", addX + addW / 2, addY + 4, Colors.BUTTON_TEXT);
        y += addH + 6;
        Gui.drawRect(contentX, y, contentX + contentW, y + 1, 0x33000000);
        y += 6;
        return y;
    }

    public boolean handleInlineClick(int mouseX, int mouseY, int contentX, int startY, int contentW) {
        int y = startY;
        // Inputs and remove buttons
        for (int i = 0; i < hotbarRows.size(); i++) {
            HotbarPresetRow row = hotbarRows.get(i);
            // label
            int labelY = y + 12; row.labelInput.setBounds(contentX, labelY, contentW, 16);
            if (row.labelInput.isMouseOver(mouseX, mouseY)) { unfocusAllInputs(); row.labelInput.beginEditing(mouseX); return true; }
            y += 12 + 16 + 6;
            // message
            int msgY = y + 12; row.triggerInput.setBounds(contentX, msgY, contentW, 16);
            if (row.triggerInput.isMouseOver(mouseX, mouseY)) { unfocusAllInputs(); row.triggerInput.beginEditing(mouseX); return true; }
            y += 12 + 16 + 6;
            // command
            int cmdY = y + 12; row.commandInput.setBounds(contentX, cmdY, contentW, 16);
            if (row.commandInput.isMouseOver(mouseX, mouseY)) { unfocusAllInputs(); row.commandInput.beginEditing(mouseX); return true; }
            y += 12 + 16 + 6;
            // remove
            int rmX = contentX, rmY = y, rmW = 70, rmH = 16;
            if (mouseX >= rmX && mouseX <= rmX + rmW && mouseY >= rmY && mouseY <= rmY + rmH) { removePresetAt(i); return true; }
            y += rmH + 8;
            y += 6;
        }
        // add button
        int addX = contentX, addY = y, addW = 90, addH = 16;
        if (mouseX >= addX && mouseX <= addX + addW && mouseY >= addY && mouseY <= addY + addH) { addCurrentHotbarAsPreset(); return true; }
        return false;
    }

    public void handleTyping(char typedChar, int keyCode) {
        for (int i = 0; i < hotbarRows.size(); i++) {
            HotbarPresetRow row = hotbarRows.get(i);
            boolean changed = false;
            if (row.labelInput.isEditing) { row.labelInput.handleKeyTyped(typedChar, keyCode); changed = true; }
            else if (row.triggerInput.isEditing) { row.triggerInput.handleKeyTyped(typedChar, keyCode); changed = true; }
            else if (row.commandInput.isEditing) { row.commandInput.handleKeyTyped(typedChar, keyCode); changed = true; }
            if (changed) {
                String nameTrim = row.labelInput.text.trim();
                String msgTrim = row.triggerInput.text.trim();
                String cmdTrim = row.commandInput.text.trim();
                if (com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE != null) {
                    com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE.updatePresetMeta(
                            i,
                            nameTrim,
                            msgTrim.isEmpty() ? null : msgTrim,
                            cmdTrim.isEmpty() ? null : cmdTrim,
                            null
                    );
                }
                return;
            }
        }
    }

    public void unfocusAllInputs() {
        for (HotbarPresetRow r : hotbarRows) { r.labelInput.isEditing = false; r.triggerInput.isEditing = false; r.commandInput.isEditing = false; }
    }

    public int computeSectionHeight(int contentW) {
        int perRow = (12 + 16 + 6) + (12 + 16 + 6) + (12 + 16 + 6) + (16 + 8) + 6; // rows + remove + separator
        int h = perRow * hotbarRows.size();
        h += 16 + 6; // add button and gap
        h += 6; // bottom separator drawn in drawInline
        return h;
    }

    private void addCurrentHotbarAsPreset() {
        if (com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE == null) return;
        com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE.addPresetFromCurrentHotbar();
        rebuildRows();
    }

    private void removePresetAt(int index) {
        if (com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE == null) return;
        com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap.INSTANCE.removePreset(index);
        rebuildRows();
    }

    private void drawCenteredString(FontRenderer font, String text, int x, int y, int color) {
        font.drawStringWithShadow(text, x - font.getStringWidth(text) / 2, y, color);
    }
}

