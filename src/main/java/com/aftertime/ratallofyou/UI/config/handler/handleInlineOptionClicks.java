package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset;
import com.aftertime.ratallofyou.UI.newui.config.ModConfigIO;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.MethodDropdown;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.utils.InlineArea;
import com.aftertime.ratallofyou.modules.dungeon.terminals.TerminalSettingsApplier;

public class handleInlineOptionClicks {
    private final ModSettingsGui gui;

    public handleInlineOptionClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleInlineOptionClicks(int mouseX, int mouseY, InlineArea ia) {
        // Focus inputs and toggle clicks (inline)
        int y = ia.contentY;
        if (gui.SelectedModule != null && "Fast Hotkey".equals(gui.SelectedModule.name)) {
            int rowH = 16; int gap = 4;
            y += 12; // after label
            if (gui.fhkPresetNameInput != null) {
                gui.fhkPresetNameInput.setBounds(ia.contentX, y, Math.max(60, ia.contentW - 65), 16);
                if (gui.fhkPresetNameInput.isMouseOver(mouseX, mouseY)) { gui.fhkPresetNameInput.beginEditing(mouseX); return; }
                int btnX = ia.contentX + ia.contentW - 60; if (mouseX >= btnX && mouseX <= btnX + 60 && mouseY >= y && mouseY <= y + 16) {
                    String name = gui.fhkPresetNameInput.text.trim(); if (!name.isEmpty()) {
                        boolean exists = false; for (FastHotkeyPreset p : AllConfig.INSTANCE.FHK_PRESETS) { if (p.name.equalsIgnoreCase(name)) { exists = true; break; } }
                        if (!exists) { AllConfig.INSTANCE.FHK_PRESETS.add(new FastHotkeyPreset(name)); AllConfig.INSTANCE.setActiveFhkPreset(AllConfig.INSTANCE.FHK_PRESETS.size() - 1); gui.fhkSelectedPreset = AllConfig.INSTANCE.FHK_ACTIVE_PRESET; gui.fhkPresetNameInput.text = ""; ModConfigIO.saveFhkPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET); gui.rebuildFastHotkeyRowsForDetail(); }
                    }
                    return;
                }
                y += 22;
            }
            y += 12; // saved header
            for (int i = 0; i < AllConfig.INSTANCE.FHK_PRESETS.size(); i++) {
                FastHotkeyPreset p = AllConfig.INSTANCE.FHK_PRESETS.get(i);
                int x = ia.contentX; int w = ia.contentW; int rowY = y; // capture for this row
                // Toggle box
                int tSize = 14; int toggleX = x; int toggleY = rowY + (rowH - tSize) / 2;
                boolean overToggle = mouseX >= toggleX && mouseX <= toggleX + tSize && mouseY >= toggleY && mouseY <= toggleY + tSize;
                // Key box
                int keyW = 80; int keyX = x + w - 60 - 6 - keyW; int keyY = rowY;
                boolean overKey = mouseX >= keyX && mouseX <= keyX + keyW && mouseY >= keyY && mouseY <= keyY + rowH;
                // Remove
                int rmW = 60; int rmX = x + w - rmW; int rmY = rowY; boolean overRemove = mouseX >= rmX && mouseX <= rmX + rmW && mouseY >= rmY && mouseY <= rmY + rowH;
                // Name/select area
                int nameX = toggleX + tSize + 6; int nameW = Math.max(40, w - 6 - tSize - 60 - 80 - 6);
                boolean overName = mouseX >= nameX && mouseX <= nameX + nameW && mouseY >= rowY && mouseY <= rowY + rowH;

                if (overToggle) {
                    // Enforce: must have a valid, non-duplicate key to enable
                    if (!p.enabled) {
                        if (p.keyCode <= 0) { gui.fhkKeyCaptureIndex = i; return; }
                        if (gui.isFhkKeyDuplicate(p.keyCode, i)) { return; }
                        p.enabled = true; ModConfigIO.saveFhkPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                    } else {
                        p.enabled = false; ModConfigIO.saveFhkPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                    }
                    // Also select this preset for editing
                    AllConfig.INSTANCE.setActiveFhkPreset(i); gui.fhkSelectedPreset = i; gui.rebuildFastHotkeyRowsForDetail();
                    return;
                }
                if (overKey) { gui.fhkKeyCaptureIndex = i; return; }
                if (overRemove) {
                    if (AllConfig.INSTANCE.FHK_PRESETS.size() > 1) {
                        AllConfig.INSTANCE.FHK_PRESETS.remove(i);
                        int newActive = Math.max(0, Math.min(AllConfig.INSTANCE.FHK_ACTIVE_PRESET - (i <= AllConfig.INSTANCE.FHK_ACTIVE_PRESET ? 1 : 0), AllConfig.INSTANCE.FHK_PRESETS.size() - 1));
                        AllConfig.INSTANCE.setActiveFhkPreset(newActive);
                        gui.fhkSelectedPreset = -1; gui.fastRows.clear();
                        ModConfigIO.saveFhkPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                    }
                    return;
                }
                if (overName) { AllConfig.INSTANCE.setActiveFhkPreset(i); gui.fhkSelectedPreset = i; gui.rebuildFastHotkeyRowsForDetail(); return; }
                y += rowH + gap; // advance to next row baseline
            }
            y += 6; // separator gap
        }
        // New: Hotbar Swap inline clicks delegated to panel
        if (gui.SelectedModule != null && "Hotbar Swap".equals(gui.SelectedModule.name)) {
            if (gui.hotbarPanel.handleInlineClick(mouseX, mouseY, ia.contentX, y, ia.contentW)) return;
            // Continue to other inputs after a small gap
            y += gui.hotbarPanel.computeSectionHeight(ia.contentW) + 12; // approximate advance for following controls
        }
        // Inputs and toggles
        int yToggle = y;
        for (Toggle toggle : gui.Toggles) {
            if (toggle.isMouseOver(mouseX, mouseY, yToggle)) {
                toggle.toggle();
                // Add back the terminal settings applier that was missing
                if (toggle.ref != null && toggle.ref.ConfigType == 4) TerminalSettingsApplier.applyFromAllConfig();
                return;
            }
            yToggle += 22;
        }
        // Handle LabelledInput clicks
        int yLI = y;
        for (Toggle ignored : gui.Toggles) yLI += 22;
        for (LabelledInput li : gui.labelledInputs) {
            if (li.isMouseOver(mouseX, mouseY, yLI)) {
                for (LabelledInput other : gui.labelledInputs) other.isEditing = false;
                li.beginEditing(mouseX);
                return;
            }
            yLI += li.getVerticalSpace();
        }
        // Handle ColorInput clicks
        int yCI = y;
        for (Toggle ignored : gui.Toggles) yCI += 22;
        for (LabelledInput li : gui.labelledInputs) yCI += li.getVerticalSpace();
        for (ColorInput ci : gui.ColorInputs) {
            int inputY = yCI + ci.height + 8;
            boolean hover = (mouseX >= ci.x + 40 && mouseX <= ci.x + ci.width && mouseY >= inputY - 2 && mouseY <= inputY + 15);
            if (hover) {
                ci.beginEditing(mouseX);
                return;
            }
            yCI += 50;
        }
        // Handle MethodDropdown clicks
        int yd = y;
        for (Toggle ignored : gui.Toggles) yd += 22;
        for (LabelledInput li : gui.labelledInputs) yd += li.getVerticalSpace();
        for (ColorInput ignored : gui.ColorInputs) yd += 50;
        for (MethodDropdown dd : gui.methodDropdowns) {
            int bx = dd.x + 100;
            int bw = dd.width - 100;
            int bh = dd.height;
            boolean inBase = mouseX >= bx && mouseX <= bx + bw && mouseY >= yd && mouseY <= yd + bh;
            if (inBase) {
                for (MethodDropdown other : gui.methodDropdowns) other.isOpen = false;
                dd.isOpen = !dd.isOpen;
                return;
            }
            if (dd.isOpen) {
                for (int i = 0; i < dd.methods.length; i++) {
                    int optionY = yd + bh + (i * bh);
                    boolean inOpt = mouseX >= bx && mouseX <= bx + bw && mouseY >= optionY && mouseY <= optionY + bh;
                    if (inOpt) {
                        dd.selectMethod(i);
                        dd.isOpen = false;
                        return;
                    }
                }
            }
            yd += 22;
        }
    }
}
