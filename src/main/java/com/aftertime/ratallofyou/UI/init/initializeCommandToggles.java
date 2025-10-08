package com.aftertime.ratallofyou.UI.init;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;

import java.util.Map;

public class initializeCommandToggles {
    private final ModSettingsGui gui;

    public initializeCommandToggles(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void initializeCommandToggles() {
        gui.Toggles.clear(); gui.labelledInputs.clear(); gui.methodDropdowns.clear(); gui.ColorInputs.clear(); if (gui.SelectedModule == null) return;
        Integer y = gui.guiTop + Dimensions.COMMAND_PANEL_Y + 30;
        // Generic: populate from the module's declared config group
        Integer group = gui.SelectedModule.configGroupIndex;
        if (group != null) {
            Map<String, BaseConfig<?>> groupMap = AllConfig.INSTANCE.ALLCONFIGS.get(group);
            if (groupMap != null) {
                for (Map.Entry<String, BaseConfig<?>> e : groupMap.entrySet()) {
                    gui.AddEntryAsOption(e, y, group);
                }
            }
        }
        // Special per-panel upkeep (no UI injection): keep Hotbar Swap rows in sync
        if (gui.SelectedModule != null && "Hotbar Swap".equals(gui.SelectedModule.name)) {
            gui.hotbarPanel.rebuildRows();
        }
        int contentHeight = 0; if (gui.useSidePanelForSelected && gui.SelectedModule != null && "Fast Hotkey".equals(gui.SelectedModule.name)) contentHeight += 12 + 22 + 12 + (AllConfig.INSTANCE.FHK_PRESETS.size() * (16 + 4));
        contentHeight += gui.Toggles.size() * 22; for (LabelledInput li : gui.labelledInputs) contentHeight += li.getVerticalSpace(); contentHeight += gui.ColorInputs.size() * 50; contentHeight += gui.methodDropdowns.size() * 22;
        int panelViewHeight = Dimensions.GUI_HEIGHT - 60 - 25;
        if (gui.useSidePanelForSelected) {
            gui.commandScroll.update(contentHeight, panelViewHeight); gui.commandScroll.updateScrollbarPosition(gui.guiLeft + Dimensions.COMMAND_PANEL_X + Dimensions.COMMAND_PANEL_WIDTH - Dimensions.SCROLLBAR_WIDTH - 2, gui.guiTop + Dimensions.COMMAND_PANEL_Y + 25, panelViewHeight);
        }
    }
}
