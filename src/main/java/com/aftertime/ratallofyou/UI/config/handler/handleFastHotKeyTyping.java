package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.FastRow;
import org.lwjgl.input.Keyboard;

public class handleFastHotKeyTyping {
    private final ModSettingsGui gui;

    public handleFastHotKeyTyping(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleFastHotKeyTyping(char typedChar, int keyCode) {
        // Handle key capture first
        if (gui.fhkKeyCaptureIndex >= 0) {
            if (keyCode == Keyboard.KEY_ESCAPE) { gui.fhkKeyCaptureIndex = -1; return; }
            if (keyCode > 0) {
                // Validate: no duplicates, valid name, not NONE
                String name = Keyboard.getKeyName(keyCode);
                if (name != null && !name.trim().isEmpty() && !"NONE".equalsIgnoreCase(name)) {
                    if (!gui.isFhkKeyDuplicate(keyCode, gui.fhkKeyCaptureIndex)) {
                        FastHotkeyPreset p = AllConfig.INSTANCE.FHK_PRESETS.get(gui.fhkKeyCaptureIndex);
                        p.keyCode = keyCode;
                        // Auto-enable now that key is valid and unique
                        p.enabled = true;
                        ConfigIO.INSTANCE.SaveFastHotKeyPresets(AllConfig.INSTANCE.FHK_PRESETS, AllConfig.INSTANCE.FHK_ACTIVE_PRESET);
                        // Auto-select this preset as active
                        AllConfig.INSTANCE.setActiveFhkPreset(gui.fhkKeyCaptureIndex); gui.fhkSelectedPreset = gui.fhkKeyCaptureIndex; gui.rebuildFastHotkeyRowsForDetail();
                        gui.fhkKeyCaptureIndex = -1; return;
                    } else {
                        // Duplicate: keep capturing until a unique key is pressed
                        return;
                    }
                } else {
                    // invalid key, keep capturing
                    return;
                }
            }
            return;
        }
        // Existing text inputs
        gui.handleAllInputTyping(typedChar, keyCode);
        for (FastRow row : gui.fastRows) {
            if (row.labelInput.isEditing) { row.labelInput.handleKeyTyped(typedChar, keyCode); return; }
            if (row.commandInput.isEditing) { row.commandInput.handleKeyTyped(typedChar, keyCode); return; }
        }
    }
}
