package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.MethodDropdown;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;

public class handleButtonClicks {
    private final ModSettingsGui gui;

    public handleButtonClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public boolean handleButtonClicks(int mouseX, int mouseY) {
        if (gui.SelectedModule == null) return false;
        int y = gui.guiTop + Dimensions.COMMAND_PANEL_Y + 30 - gui.commandScroll.getOffset();
        for (Toggle ignored : gui.Toggles) y += 22;
        for (LabelledInput li : gui.labelledInputs) y += li.getVerticalSpace();
        for (ColorInput ignored : gui.ColorInputs) y += 50;
        for (MethodDropdown ignored : gui.methodDropdowns) y += 22;
        return false;
    }
}
