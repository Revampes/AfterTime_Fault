package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;

public class handleScrollbarDrag {
    private final ModSettingsGui gui;

    public handleScrollbarDrag(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleScrollbarDrag(int mouseX, int mouseY) {
        if (gui.mainScroll.isDragging) gui.mainScroll.handleDrag(mouseX, mouseY, () -> gui.buildModuleButtons());
        if ((gui.useSidePanelForSelected || (gui.optionsInline && gui.SelectedModule != null &&
                "Fast Hotkey".equals(gui.SelectedModule.name) && gui.fhkSelectedPreset >= 0)) &&
                gui.commandScroll.isDragging) gui.commandScroll.handleDrag(mouseX, mouseY, null);
    }
}
