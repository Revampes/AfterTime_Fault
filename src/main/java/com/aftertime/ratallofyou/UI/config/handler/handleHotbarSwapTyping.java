package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;

public class handleHotbarSwapTyping {
    private final ModSettingsGui gui;

    public handleHotbarSwapTyping(ModSettingsGui gui) {
        this.gui = gui;
    }

    // New: Hotbar Swap typing handler delegates to panel
    public void handleHotbarSwapTyping(char typedChar, int keyCode) {
        // Update any focused hotbar preset input and propagate to HotbarSwap
        gui.handleAllInputTyping(typedChar, keyCode);
        gui.hotbarPanel.handleTyping(typedChar, keyCode);
    }
}
