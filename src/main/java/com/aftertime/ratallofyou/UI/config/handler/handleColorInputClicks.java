package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;

public class handleColorInputClicks {
    private final ModSettingsGui gui;

    public handleColorInputClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public boolean handleColorInputClicks(int mouseX, int mouseY) {
        if (gui.SelectedModule == null) return false; int y = gui.guiTop + Dimensions.COMMAND_PANEL_Y + 30 - gui.commandScroll.getOffset();
        for (Toggle ignored : gui.Toggles) y += 22; for (LabelledInput li : gui.labelledInputs) y += li.getVerticalSpace();
        for (ColorInput ci : gui.ColorInputs) { int inputY = y + ci.height + 8; boolean hover = (mouseX >= ci.x + 40 && mouseX <= ci.x + ci.width && mouseY >= inputY - 2 && mouseY <= inputY + 15); if (hover) { ci.beginEditing(mouseX); return true; } y += 50; }
        return false;
    }
}
