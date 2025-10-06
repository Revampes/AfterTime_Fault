package com.aftertime.ratallofyou.UI.config.drawMethod;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.commonConstant.Colors;

public class drawScrollbar {
    private final ModSettingsGui gui;

    public drawScrollbar(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void drawScrollbars() {
        if (gui.mainScroll.shouldRenderScrollbar()) gui.mainScroll.drawScrollbar(Colors.SCROLLBAR, Colors.SCROLLBAR_HANDLE);
        if (gui.showCommandSettings && gui.useSidePanelForSelected && gui.commandScroll.shouldRenderScrollbar()) {
            gui.commandScroll.drawScrollbar(Colors.COMMAND_SCROLLBAR, Colors.COMMAND_SCROLLBAR_HANDLE);
        }
        // Inline Fast Hotkey: draw scrollbar for right detail panel when open
        if (gui.showCommandSettings && gui.optionsInline && gui.SelectedModule != null && "Fast Hotkey".equals(gui.SelectedModule.name) && gui.fhkSelectedPreset >= 0 && gui.commandScroll.shouldRenderScrollbar()) {
            gui.commandScroll.drawScrollbar(Colors.COMMAND_SCROLLBAR, Colors.COMMAND_SCROLLBAR_HANDLE);
        }
    }
}
