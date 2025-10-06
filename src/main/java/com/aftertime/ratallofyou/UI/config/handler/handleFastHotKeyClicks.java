package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.UI.config.OptionElements.FastRow;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;

import java.util.ArrayList;
import java.util.List;

public class handleFastHotKeyClicks {
    private final ModSettingsGui gui;

    public handleFastHotKeyClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleFastHotKeyClicks(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) return;
        int panelX = gui.guiLeft + Dimensions.COMMAND_PANEL_X; int panelY = gui.guiTop + Dimensions.COMMAND_PANEL_Y; int panelWidth = Dimensions.COMMAND_PANEL_WIDTH;
        int leftX = panelX + 5; int leftW = panelWidth - 10; int leftContentY = panelY + 25 - gui.commandScroll.getOffset();
        boolean isInline = gui.optionsInline && gui.SelectedModule != null && "Fast Hotkey".equals(gui.SelectedModule.name);
        int rightPanelX = isInline ? gui.getInlineDetailX() : (panelX + panelWidth + 6);
        int rightPanelW = 170;
        int areaTopY = panelY; int areaBotY = panelY + (Dimensions.GUI_HEIGHT - 60);
        boolean clickInRight = mouseX >= rightPanelX && mouseX <= rightPanelX + rightPanelW && mouseY >= areaTopY && mouseY <= areaBotY;
        boolean clickInLeft = !isInline && mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= areaTopY && mouseY <= areaBotY;

        // Right-side Preset Editor clicks (both modes)
        if (clickInRight) {
            int detailX = rightPanelX; int x = detailX + 5; int contentY = panelY + 25 - gui.commandScroll.getOffset();
            for (int i = 0; i < gui.fastRows.size(); i++) {
                FastRow row = gui.fastRows.get(i); int rowTop = contentY + i * Dimensions.FH_ROW_HEIGHT;
                int labelInputY = rowTop + 14; int commandInputY = labelInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y + 16;
                if (row.labelInput.isMouseOver(mouseX, mouseY, labelInputY)) { gui.unfocusAllFastInputs(); row.labelInput.beginEditing(mouseX, row.labelInput.x); return; }
                if (row.commandInput.isMouseOver(mouseX, mouseY, commandInputY)) { gui.unfocusAllFastInputs(); row.commandInput.beginEditing(mouseX, row.commandInput.x); return; }
                int removeX = x; int removeY = commandInputY + Dimensions.FH_INPUT_HEIGHT + Dimensions.FH_GAP_Y;
                if (mouseX >= removeX && mouseX <= removeX + Dimensions.FH_REMOVE_WIDTH && mouseY >= removeY && mouseY <= removeY + Dimensions.FH_REMOVE_HEIGHT) {
                    if (i < AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.size()) {
                        AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.remove(i);
                        List<FastHotkeyEntry> old = new ArrayList<>(AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES);
                        List<FastHotkeyEntry> rebuilt = new ArrayList<>();
                        for (int j = 0; j < old.size(); j++) rebuilt.add(new FastHotkeyEntry(old.get(j).label, old.get(j).command, j));
                        java.util.List<FastHotkeyEntry> list = AllConfig.INSTANCE.FHK_PRESETS.get(AllConfig.INSTANCE.FHK_ACTIVE_PRESET).entries;
                        list.clear(); list.addAll(rebuilt);
                        AllConfig.INSTANCE.setActiveFhkPreset(AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                        ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                        gui.rebuildFastHotkeyRowsForDetail();
                    }
                    return;
                }
            }
            int addY = contentY + gui.fastRows.size() * Dimensions.FH_ROW_HEIGHT + 8; int addW = 60;
            if (mouseX >= x && mouseX <= x + addW && mouseY >= addY && mouseY <= addY + Dimensions.FH_ADD_HEIGHT) {
                int idx = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.size(); AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.add(new FastHotkeyEntry("", "", idx));
                ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                gui.rebuildFastHotkeyRowsForDetail();
            }
            return;
        }

        // Left side (only in side-panel mode): presets list + appearance options
        if (clickInLeft) {
            // Preset input + confirm
            int y = leftContentY + 12; if (gui.fhkPresetNameInput != null) {
                gui.fhkPresetNameInput.setBounds(leftX, y, Math.max(60, leftW - 65), 16);
                if (gui.fhkPresetNameInput.isMouseOver(mouseX, mouseY)) { gui.unfocusAllFastInputs(); gui.fhkPresetNameInput.beginEditing(mouseX); return; }
                int btnX = leftX + leftW - 60; if (mouseX >= btnX && mouseX <= btnX + 60 && mouseY >= y && mouseY <= y + 16) {
                    String name = gui.fhkPresetNameInput.text.trim(); if (!name.isEmpty()) {
                        boolean exists = false; for (FastHotkeyPreset p : AllConfig.INSTANCE.FHK_PRESETS) { if (p.name.equalsIgnoreCase(name)) { exists = true; break; } }
                        if (!exists) { AllConfig.INSTANCE.FHK_PRESETS.add(new FastHotkeyPreset(name)); AllConfig.INSTANCE.setActiveFhkPreset(AllConfig.INSTANCE.FHK_PRESETS.size() - 1); gui.fhkSelectedPreset = AllConfig.INSTANCE.FHK_ACTIVE_PRESET; gui.fhkPresetNameInput.text = ""; ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET); gui.rebuildFastHotkeyRowsForDetail(); }
                    }
                    return;
                }
                y += 22;
            }
            // Saved list open/remove
            y += 12; int presetBtnH = 16;
            for (int i = 0; i < AllConfig.INSTANCE.FHK_PRESETS.size(); i++) {
                int openW = Math.max(60, leftW - 70); int openX = leftX; int openY = y; int rmX = leftX + leftW - 60;
                if (mouseX >= openX && mouseX <= openX + openW && mouseY >= openY && mouseY <= openY + presetBtnH) { AllConfig.INSTANCE.setActiveFhkPreset(i); gui.fhkSelectedPreset = i; gui.rebuildFastHotkeyRowsForDetail(); return; }
                if (mouseX >= rmX && mouseX <= rmX + 60 && mouseY >= openY && mouseY <= openY + presetBtnH) {
                    if (AllConfig.INSTANCE.FHK_PRESETS.size() > 1) {
                        AllConfig.INSTANCE.FHK_PRESETS.remove(i);
                        int newActive = Math.max(0, Math.min(AllConfig.INSTANCE.FHK_ACTIVE_PRESET - (i <= AllConfig.INSTANCE.FHK_ACTIVE_PRESET ? 1 : 0), AllConfig.INSTANCE.FHK_PRESETS.size() - 1));
                        AllConfig.INSTANCE.setActiveFhkPreset(newActive);
                        gui.fhkSelectedPreset = -1; gui.fastRows.clear();
                        ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                    }
                    return;
                }
                y += presetBtnH + 4;
            }
            // Appearance options (toggles/inputs/colors)
            if (gui.handleLabelledInputClicks(mouseX, mouseY)) return;
            if (gui.handleColorInputClicks(mouseX, mouseY)) return;
            int yToggle = leftContentY + 12 + 22 + 12 + (AllConfig.INSTANCE.FHK_PRESETS.size() * (presetBtnH + 4)) + 6;
            for (Toggle t : gui.Toggles) { if (t.isMouseOver(mouseX, mouseY, yToggle)) { t.toggle(); return; } yToggle += 22; }
        }
    }
}
