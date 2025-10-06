package com.aftertime.ratallofyou.UI.config.handler;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import com.aftertime.ratallofyou.UI.config.OptionElements.LabelledInput;
import org.lwjgl.input.Keyboard;

public class handleMarkLocationTyping {
    private final ModSettingsGui gui;

    public handleMarkLocationTyping(ModSettingsGui gui) {
        this.gui = gui;
    }

    public void handleMarkLocationTyping(char typedChar, int keyCode) {
        LabelledInput hotkeyInput = null;
        for (LabelledInput li : gui.labelledInputs) {
            if (li.ref != null && li.ref.ConfigType == 17 && "marklocation_hotkey".equals(li.ref.Key)) {
                hotkeyInput = li;
                break;
            }
        }
        if (hotkeyInput != null && hotkeyInput.isEditing) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                hotkeyInput.setDisplayText("Unbound");
                BaseConfig<?> cfg = AllConfig.INSTANCE.MARKLOCATION_CONFIGS.get("marklocation_hotkey");
                if (cfg != null) { @SuppressWarnings("unchecked") BaseConfig<Object> c = (BaseConfig<Object>) cfg; c.Data = 0; }
                ConfigIO.INSTANCE.SetConfig("17,marklocation_hotkey", 0);
                hotkeyInput.isEditing = false;
                return;
            }
            if (keyCode > 0) {
                String name = Keyboard.getKeyName(keyCode);
                if (name != null && !name.trim().isEmpty() && !"NONE".equalsIgnoreCase(name)) {
                    hotkeyInput.setDisplayText(name);
                    BaseConfig<?> cfg = AllConfig.INSTANCE.MARKLOCATION_CONFIGS.get("marklocation_hotkey");
                    if (cfg != null) { @SuppressWarnings("unchecked") BaseConfig<Object> c = (BaseConfig<Object>) cfg; c.Data = keyCode; }
                    ConfigIO.INSTANCE.SetConfig("17,marklocation_hotkey", keyCode);
                    hotkeyInput.isEditing = false;
                    return;
                }
                // invalid key -> ignore until a valid one pressed
                return;
            }
            return;
        }
        // Not editing hotkey -> route to default input handling
        gui.handleAllInputTyping(typedChar, keyCode);
    }
}
