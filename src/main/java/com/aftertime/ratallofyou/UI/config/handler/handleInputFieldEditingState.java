package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ColorInput;
import net.minecraftforge.fml.common.Mod;

public class handleInputFieldEditingState {
    private final ModSettingsGui gui;

    public handleInputFieldEditingState(ModSettingsGui gui) {
        this.gui = gui;
    }

    // Input and scroll helpers
    public void handleInputFieldEditingState() {
        for (ColorInput c : gui.ColorInputs) c.unfocus();
        if (gui.showCommandSettings && gui.SelectedModule != null && "Fast Hotkey".equals(gui.SelectedModule.name)) gui.unfocusAllFastInputs();
        if (gui.showCommandSettings && gui.SelectedModule != null && "Hotbar Swap".equals(gui.SelectedModule.name)) gui.unfocusAllHotbarInputs(); }
}
