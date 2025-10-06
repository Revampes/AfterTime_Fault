package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;

public class handleScrollbarClicks {
    private final ModSettingsGui gui;

    public handleScrollbarClicks(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleScrollbarClicks(int mouseX, int mouseY) {
        if (gui.showCommandSettings && gui.useSidePanelForSelected && gui.commandScroll.checkScrollbarClick(mouseX, mouseY)) return;
        if (gui.showCommandSettings && gui.optionsInline && gui.SelectedModule != null &&
            "Fast Hotkey".equals(gui.SelectedModule.name) && gui.fhkSelectedPreset >= 0 &&
            gui.commandScroll.checkScrollbarClick(mouseX, mouseY)) return;
        if (gui.mainScroll.checkScrollbarClick(mouseX, mouseY)) return; }
}
