package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;

public class handleLabelledInputClicks {
    private final ModSettingsGui gui;

    public handleLabelledInputClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public boolean handleLabelledInputClicks(int mouseX, int mouseY) {
        if (gui.SelectedModule == null) return false; int y = gui.guiTop + Dimensions.COMMAND_PANEL_Y + 30 - gui.commandScroll.getOffset();
        for (Toggle ignored : gui.Toggles) y += 22;
        for (LabelledInput li : gui.labelledInputs) { if (li.isMouseOver(mouseX, mouseY, y)) { for (LabelledInput other : gui.labelledInputs) other.isEditing = false; li.beginEditing(mouseX); return true; } y += li.getVerticalSpace(); }
        return false;
    }
}
