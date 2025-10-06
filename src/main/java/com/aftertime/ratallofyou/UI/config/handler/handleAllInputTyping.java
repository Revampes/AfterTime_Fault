package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;

public class handleAllInputTyping {
    private final ModSettingsGui gui;

    public handleAllInputTyping(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleAllInputTyping(char typedChar, int keyCode) {
        if (!gui.showCommandSettings) return;
        for (ColorInput t : gui.ColorInputs) t.handleKeyTyped(typedChar, keyCode);
        for (LabelledInput t : gui.labelledInputs) t.handleKeyTyped(typedChar, keyCode);
    }
}
