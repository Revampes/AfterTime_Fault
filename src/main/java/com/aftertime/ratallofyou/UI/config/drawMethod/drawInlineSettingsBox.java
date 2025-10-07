package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.MethodDropdown;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;
import com.aftertime.ratallofyou.UI.utils.InlineArea;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.input.Keyboard;

public class drawInlineSettingsBox {
    private final ModSettingsGui gui;

    public drawInlineSettingsBox(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawInlineSettingsBox(int mouseX, int mouseY) {
        FontRenderer fontRenderer = gui.getFontRendererObj();
        InlineArea ia = gui.getInlineAreaForSelected(); if (ia == null) return;
        gui.drawRect(ia.boxX, ia.boxY, ia.boxX + ia.boxW, ia.boxY + ia.boxH, Colors.COMMAND_PANEL);
        gui.drawRect(ia.boxX - 1, ia.boxY - 1, ia.boxX + ia.boxW + 1, ia.boxY + ia.boxH + 1, Colors.COMMAND_BORDER);
        gui.drawCenteredString(fontRenderer, gui.SelectedModule.name + " Settings", ia.boxX + ia.boxW / 2, ia.boxY + 6, Colors.COMMAND_TEXT);
        int y = ia.contentY;
        if ("Fast Hotkey".equals(gui.SelectedModule.name)) {
            // Inline fast hotkey: presets list + appearance
            fontRenderer.drawStringWithShadow("Create settings:", ia.contentX, y, Colors.COMMAND_TEXT); y += 12;
            if (gui.fhkPresetNameInput != null) {
                gui.fhkPresetNameInput.setBounds(ia.contentX, y, Math.max(60, ia.contentW - 65), 16);
                gui.fhkPresetNameInput.draw(mouseX, mouseY);
                int btnX = ia.contentX + ia.contentW - 60;
                gui.drawRect(btnX, y, btnX + 60, y + 16, Colors.BUTTON_GREEN);
                gui.drawCenteredString(fontRenderer, "Confirm", btnX + 30, y + 4, Colors.BUTTON_TEXT);
                y += 22;
            }
            fontRenderer.drawStringWithShadow("Saved settings:", ia.contentX, y, Colors.COMMAND_TEXT); y += 12;
            int rowH = 16; int gap = 4;
            for (int i = 0; i < AllConfig.INSTANCE.FHK_PRESETS.size(); i++) {
                FastHotkeyPreset p = AllConfig.INSTANCE.FHK_PRESETS.get(i);
                int x = ia.contentX; int w = ia.contentW; int h = rowH; int rowY = y;
                // Toggle 14x14
                int tSize = 14; int toggleX = x; int toggleY = rowY + (h - tSize) / 2;
                int toggleColor = p.enabled ? Colors.BUTTON_GREEN : Colors.BUTTON_RED;
                gui.drawRect(toggleX, toggleY, toggleX + tSize, toggleY + tSize, toggleColor);
                // Name area clickable to select preset
                int nameX = toggleX + tSize + 6; int nameW = Math.max(40, w - 6 - tSize - 60 - 80 - 6); // leave space for remove(60) + key(80) + gaps
                int nameCenterY = rowY + 4;
                String nm = p.name + (i == AllConfig.INSTANCE.FHK_ACTIVE_PRESET ? "  (Active)" : "");
                fontRenderer.drawStringWithShadow(nm, nameX, nameCenterY, Colors.COMMAND_TEXT);
                // Keybind box 80px
                int keyW = 80; int keyX = x + w - 60 - 6 - keyW; int keyY = rowY;
                gui.drawRect(keyX, keyY, keyX + keyW, keyY + h, Colors.INPUT_BG);
                String keyLabel;
                if (gui.fhkKeyCaptureIndex == i) keyLabel = "Press a key...";
                else keyLabel = p.keyCode <= 0 ? "Unbound" : Keyboard.getKeyName(p.keyCode);
                if (keyLabel == null || keyLabel.trim().isEmpty()) keyLabel = "Unknown";
                fontRenderer.drawStringWithShadow(keyLabel, keyX + 4, keyY + 4, Colors.INPUT_FG);
                // Remove button 60px
                int rmW = 60; int rmX = x + w - rmW; int rmY = rowY;
                gui.drawRect(rmX, rmY, rmX + rmW, rmY + h, Colors.BUTTON_RED);
                gui.drawCenteredString(fontRenderer, "Remove", rmX + rmW / 2, rmY + 4, Colors.BUTTON_TEXT);
                y += h + gap;
            }
            gui.drawRect(ia.contentX, y, ia.contentX + ia.contentW, y + 1, 0x33000000); y += 6;
        }
        // New: Hotbar Swap inline drawing delegated to panel
        if ("Hotbar Swap".equals(gui.SelectedModule.name)) {
            y = gui.hotbarPanel.drawInline(mouseX, mouseY, ia.contentX, y, ia.contentW, fontRenderer);
        }

        // Draw general options for all modules (toggles, inputs, colors) - but NOT dropdowns yet
        for (Toggle toggle : gui.Toggles) {
            toggle.draw(mouseX, mouseY, y, fontRenderer);
            y += 22;
        }
        for (LabelledInput li : gui.labelledInputs) {
            li.draw(mouseX, mouseY, y, fontRenderer);
            y += li.getVerticalSpace();
        }
        for (ColorInput ci : gui.ColorInputs) {
            ci.draw(mouseX, mouseY, y, fontRenderer);
            y += 50;
        }

        // Record baseline for the inline dropdown overlay and draw bases
        int baseY = y;
        gui.inlineDropdownBaseY = baseY;
        for (MethodDropdown dd : gui.methodDropdowns) {
            dd.drawBase(mouseX, mouseY, baseY, fontRenderer);
            baseY += 22;
        }
    }
}
