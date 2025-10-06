package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.UIHighlighter;
import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.ModuleButton;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Mouse;

public class handleModuleButtonClick {
    private final ModSettingsGui gui;

    public handleModuleButtonClick(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleModuleButtonClick(ModuleButton moduleBtn, int mouseX, int mouseY) {
        ModuleInfo module = moduleBtn.getModule();
        if ("Move GUI Position".equals(module.name)) UIHighlighter.enterMoveMode(Minecraft.getMinecraft().currentScreen);

        // Check if this is a right click (mouse button 1) for settings
        if (Mouse.isButtonDown(1)) { // Right click
            if (!moduleBtn.hasSettings) {
                // Show error message for modules without settings - will be handled in drawScreen
                gui.showNoSettingsError = module.name;
                gui.noSettingsErrorTime = System.currentTimeMillis();
                return;
            }

            // Open settings for modules that have them
            if (gui.showCommandSettings && gui.SelectedModule == module) {
                gui.showCommandSettings = false;
                gui.SelectedModule = null;
                gui.useSidePanelForSelected = false;
                gui.optionsInline = false;
                gui.buildModuleButtons();
                return;
            }
            gui.SelectedModule = module;
            gui.showCommandSettings = true;
            if ("Fast Hotkey".equals(gui.SelectedModule.name)) {
                gui.useSidePanelForSelected = false;
                gui.optionsInline = true;
                gui.fhkSelectedPreset = AllConfig.INSTANCE.FHK_ACTIVE_PRESET;
                gui.rebuildFastHotkeyRowsForDetail();
            } else {
                gui.useSidePanelForSelected = false;
                gui.optionsInline = true;
            }
            gui.initializeCommandToggles();
            gui.buildModuleButtons();
            return;
        } else {
            // Left click - toggle module on/off
            boolean wasEnabled = module.Data;
            module.Data = !module.Data;
            if (wasEnabled && !module.Data && gui.SelectedModule == module) {
                gui.showCommandSettings = false;
                gui.SelectedModule = null;
                gui.useSidePanelForSelected = false;
                gui.optionsInline = false;
                gui.buildModuleButtons();
            }
        }
    }
}
