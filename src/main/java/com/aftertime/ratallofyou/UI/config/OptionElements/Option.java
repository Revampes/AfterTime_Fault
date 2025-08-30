package com.aftertime.ratallofyou.UI.config.OptionElements;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.PropertyRef;
import net.minecraft.client.gui.FontRenderer;

public abstract class Option<T> extends GuiElement {
    public final String name;
    protected final String description;
    protected T Data;
    public PropertyRef ref;

    public Option(PropertyRef ref, String name, String description, T t, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.name = name; this.description = description;
        this.Data = t;
        this.ref = ref;
    }
    public abstract void draw(int mouseX, int mouseY, int yPos, FontRenderer fr);
    @SuppressWarnings("unchecked")
    protected void OnValueChange()
    {
        // Special handling for Fast Hotkey editor inputs
        if (ref.ConfigType == 99) {
            // ref.Key format: "<index>_label" or "<index>_command"
            String key = ref.Key;
            int underscore = key.indexOf('_');
            if (underscore > 0) {
                try {
                    int idx = Integer.parseInt(key.substring(0, underscore));
                    String field = key.substring(underscore + 1);
                    if (idx >= 0 && idx < AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.size()) {
                        com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry e = AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES.get(idx);
                        if ("label".equals(field)) {
                            e.label = String.valueOf(Data);
                        } else if ("command".equals(field)) {
                            e.command = String.valueOf(Data);
                        }
                        // Persist presets (also mirrors legacy keys)
                        com.aftertime.ratallofyou.UI.config.ConfigIO.INSTANCE.SaveFastHotKeyPresets(
                                AllConfig.INSTANCE.FHK_PRESETS,
                                AllConfig.INSTANCE.FHK_ACTIVE_PRESET
                        );
                    }
                } catch (NumberFormatException ignored) { }
            }
            return;
        }

        com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<?> cfgBase =
                AllConfig.INSTANCE.ALLCONFIGS
                        .get(ref.ConfigType)
                        .get(ref.Key);
        if (cfgBase == null) return;
        java.lang.reflect.Type targetType = cfgBase.type;

        Object newVal = Data;
        // Sanitize and coerce text inputs into the target primitive types
        try {
            if (targetType == Integer.class && !(Data instanceof Integer)) {
                String s = String.valueOf(Data).trim();
                // strip commas and spaces
                s = s.replace(",", "").replace(" ", "");
                if (!s.isEmpty()) newVal = Integer.parseInt(s);
            } else if (targetType == Float.class && !(Data instanceof Float)) {
                String s = String.valueOf(Data).trim();
                s = s.replace(",", "").replace(" ", "");
                if (!s.isEmpty()) newVal = Float.parseFloat(s);
            } else if (targetType == Boolean.class && !(Data instanceof Boolean)) {
                String s = String.valueOf(Data).trim();
                newVal = Boolean.parseBoolean(s);
            }
        } catch (Exception ignored) {
            // If parse fails, keep old cfg value by not overwriting
            return;
        }

        // Assign with correct type
        com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<Object> cfg =
                (com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig<Object>) cfgBase;
        cfg.Data = newVal;

        // Persist immediately to properties so other systems and restarts see the change
        String compositeKey = ref.ConfigType + "," + ref.Key;
        com.aftertime.ratallofyou.UI.config.ConfigIO.INSTANCE.SetConfig(compositeKey, newVal);
    }

}
