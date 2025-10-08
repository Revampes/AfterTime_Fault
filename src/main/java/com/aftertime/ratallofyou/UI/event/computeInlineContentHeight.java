package com.aftertime.ratallofyou.UI.event;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;

public class computeInlineContentHeight {
    private final ModSettingsGui gui;

    public computeInlineContentHeight(ModSettingsGui gui) {
        this.gui = gui;
    }

    public int computeInlineContentHeight() {
        if (gui.SelectedModule != null && "Fast Hotkey".equals(gui.SelectedModule.name)) {
            int h = 34;
            int rowH = 16;
            int gap = 4;
            h += 12 + (AllConfig.INSTANCE.FHK_PRESETS.size() * (rowH + gap));
            h += 6;
            h += gui.Toggles.size() * 22;
            for (LabelledInput li : gui.labelledInputs) h += li.getVerticalSpace();
            h += gui.ColorInputs.size() * 50;
            h += gui.methodDropdowns.size() * 22;
            return h + 6;
        }

        if (gui.SelectedModule != null && "Hotbar Swap".equals(gui.SelectedModule.name)) {
            int listW = Dimensions.GUI_WIDTH - 120 - Dimensions.SCROLLBAR_WIDTH;
            int contentW = (listW - 8) - 6 * 2;
            int h = gui.hotbarPanel.computeSectionHeight(contentW);
            h += gui.Toggles.size() * 22;
            for (LabelledInput li : gui.labelledInputs) h += li.getVerticalSpace();
            h += gui.ColorInputs.size() * 50;
            h += gui.methodDropdowns.size() * 22;
            return h + 6;
        }
        int h = gui.Toggles.size() * 22;
        for (LabelledInput li : gui.labelledInputs) h += li.getVerticalSpace();
        h += gui.ColorInputs.size() * 50;
        h += gui.methodDropdowns.size() * 22;
        return h + 6;
    }
}
