package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.Toggle;
import com.aftertime.ratallofyou.UI.config.commonConstant.Dimensions;
import com.aftertime.ratallofyou.UI.utils.InlineArea;

public class handleCommandToggleClicks {
    private final ModSettingsGui gui;

    public handleCommandToggleClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleCommandToggleClicks(int mouseX, int mouseY) {
        if (!gui.showCommandSettings) return;
        if (gui.optionsInline && gui.SelectedModule != null) { InlineArea ia = gui.getInlineAreaForSelected(); if (ia != null) gui.handleInlineOptionClicks(mouseX, mouseY, ia); return; }
        if (gui.handleLabelledInputClicks(mouseX, mouseY)) return;
        if (gui.handleDropdownClicks(mouseX, mouseY)) return;
        if (gui.handleColorInputClicks(mouseX, mouseY)) return;

        // Handle button clicks for side panel mode
        if (gui.handleButtonClicks(mouseX, mouseY)) return;

        if (gui.SelectedModule == null) return;
        int y = gui.guiTop + Dimensions.COMMAND_PANEL_Y + 30 - gui.commandScroll.getOffset();
        for (Toggle toggle : gui.Toggles) {
            if (toggle.isMouseOver(mouseX, mouseY, y)) {
                toggle.toggle();
                return;
            }
            y += 22;
        }
    }
}
