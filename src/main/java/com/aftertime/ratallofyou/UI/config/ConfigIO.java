package com.aftertime.ratallofyou.UI.config;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.DataType_DropDown;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry;
import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ConfigIO {

    public boolean AutoSave = false;
    public static ConfigIO INSTANCE = new ConfigIO();
    public final File CONFIG_FILE = new File("config/ratallofyou/Config.cfg");
    public final File FastHotKey_FILE = new File("config/ratallofyou/FHK.cfg");


    public Properties properties;
    public Properties FastHotKeyProperties;
    public void InitializeConfigs() {
        Properties prop = LoadProperties(CONFIG_FILE);
        Properties fastHotKeyProp = LoadProperties(FastHotKey_FILE);
        // Set fields before delegating so GetConfig() has non-null properties
        properties = prop;
        FastHotKeyProperties = fastHotKeyProp;
        AllConfig.INSTANCE.LoadFromProperty(prop, fastHotKeyProp);


    }

    public void SetConfig(String Key, Object value) {
        if (value instanceof Boolean) {
            SetBool(Key, (Boolean) value);
        } else if (value instanceof String) {
            SetString(Key, (String) value);
        } else if (value instanceof Float) {
            SetFloat(Key, (Float) value);
        } else if (value instanceof Integer) {
            SetInt(Key, (Integer) value);
        } else if (value instanceof Color) {
            SetColor(Key, (Color) value);
        } else if (value instanceof DataType_DropDown) {
            SetDropDown(Key, (DataType_DropDown) value);
        } else if (value instanceof int[]) {
            int[] pos = (int[]) value;
            SetUIPosition(Key, pos[0], pos[1]);
        } else if (value instanceof UIPosition) {
            UIPosition p = (UIPosition) value;
            SetUIPosition(Key, p.x, p.y);
        }
    }
    public Object GetConfig(String Key,Type type)
    {
        if(type == Boolean.class) {
            return GetBool(Key);
        } else if(type == String.class) {
            return GetString(Key);
        } else if(type == Float.class) {
            return GetFloat(Key);
        } else if(type == Integer.class) {
            return GetInt(Key);
        } else if(type == Color.class) {
            return GetColor(Key);
        } else if(type == DataType_DropDown.class) {
            return GetDropDown(Key);
        } else if(type == int[].class) {
            return GetUIPosition(Key);
        } else if (type == UIPosition.class) {
            int[] xy = GetUIPosition(Key);
            return xy == null ? null : new UIPosition(xy[0], xy[1]);
        }
        return null;
    }
    //Set Property Field
    public void SetString( String Key, String value) {
        properties.setProperty( Key, value);
    }
    public void SetBool(String Key, boolean value) {
        String Value = Boolean.toString(value);
        properties.setProperty(Key,Value);
    }
    public void SetFloat(String Key, float value) {
        String Value = Float.toString(value);
        properties.setProperty(Key,Value);
    }
    public void SetInt(String Key, int value) {
        String Value = Integer.toString(value);
        properties.setProperty(Key,Value);
    }
    public void SetColor(String Key, Color value) {
        String Value = Integer.toString(value.getRGB());
        properties.setProperty(Key,Value);
    }
    public void SetDropDown( String Key, DataType_DropDown dropdown) {
        String options = String.join(",",dropdown.options);
        int selectedIndex = dropdown.selectedIndex;
        properties.setProperty( Key + "_options", options);
        properties.setProperty( Key + "_index", Integer.toString(selectedIndex));
    }
    public void SetDropDownSelect( String Key, int selectedIndex) {
        properties.setProperty( Key + "_index", Integer.toString(selectedIndex));

    }
    public void SetUIPosition( String Key,int x, int y) {
        properties.setProperty( Key, x + "," + y);

    }

    //Get Property Field
    public String GetString(String Key) {
        return properties.getProperty(Key);
    }
    public Boolean GetBool(String Key) {
        String Value = properties.getProperty(Key);
        if (Value == null) return null;
        return Boolean.parseBoolean(Value);
    }
    public Float GetFloat(String Key) {
        String Value = properties.getProperty( Key);
        if (Value == null) return null;
        try {
            return Float.parseFloat(Value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
    public DataType_DropDown GetDropDown( String Key) {
        String options = properties.getProperty( Key + "_options");
        String indexStr = properties.getProperty( Key + "_index");
        if (options == null || indexStr == null) return null;
        String[] optionArray = options.split(",");
        int selectedIndex;
        try {
            selectedIndex = Integer.parseInt(indexStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            selectedIndex = 0; // Default to first option if parsing fails
        }
        return new DataType_DropDown(selectedIndex, optionArray);
    }
    public Integer GetInt( String Key) {
        String Value = properties.getProperty( Key);
        if (Value == null) return null;
        try {
            return Integer.parseInt(Value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
    public Color GetColor( String Key) {
        String Value = properties.getProperty( Key);
        if (Value == null) return null;
        try {
            // Preserve alpha channel; stored value is ARGB from Color#getRGB()
            return new Color(Integer.parseInt(Value), true);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Legacy single-list FHK API (kept for backward compatibility)
    public java.util.List<FastHotkeyEntry> LoadFastHotKeyEntries() {
        List<FastHotkeyEntry> entries = new ArrayList<>();
        int length = Integer.parseInt(FastHotKeyProperties.getProperty("fhk_length","0"));
        for(int i = 0 ; i < length;i++)
        {
            String label = FastHotKeyProperties.getProperty("fhk_" + i + "_label");
            String command = FastHotKeyProperties.getProperty("fhk_" + i + "_command");
                FastHotkeyEntry entry = new FastHotkeyEntry(label,command,i);
                entries.add(entry);

        }
        return entries;
    }
    public void SaveFastHotKeyEntries(List<FastHotkeyEntry> entries) {
        FastHotKeyProperties.setProperty("fhk_length", String.valueOf(entries.size()));
        for (int i = 0; i < entries.size(); i++) {
            FastHotkeyEntry entry = entries.get(i);
            FastHotKeyProperties.setProperty("fhk_" + i + "_label", entry.label);
            FastHotKeyProperties.setProperty("fhk_" + i + "_command", entry.command);
        }
    }

    // New multi-preset FHK API
    public List<FastHotkeyPreset> LoadFastHotKeyPresets() {
        List<FastHotkeyPreset> presets = new ArrayList<>();
        try {
            int pCount = Integer.parseInt(FastHotKeyProperties.getProperty("fhk_preset_count", "0"));
            if (pCount <= 0) return presets;
            for (int p = 0; p < pCount; p++) {
                String name = FastHotKeyProperties.getProperty("fhk_preset_" + p + "_name", "Preset " + (p + 1));
                FastHotkeyPreset preset = new FastHotkeyPreset(name);
                // New: enabled + keyCode
                boolean enabled = Boolean.parseBoolean(FastHotKeyProperties.getProperty("fhk_preset_" + p + "_enabled", "false"));
                int keyCode = 0; try { keyCode = Integer.parseInt(FastHotKeyProperties.getProperty("fhk_preset_" + p + "_keycode", "0")); } catch (Exception ignored) {}
                preset.enabled = enabled; preset.keyCode = keyCode;
                int len = Integer.parseInt(FastHotKeyProperties.getProperty("fhk_preset_" + p + "_length", "0"));
                for (int i = 0; i < len; i++) {
                    String label = FastHotKeyProperties.getProperty("fhk_preset_" + p + "_" + i + "_label", "");
                    String cmd = FastHotKeyProperties.getProperty("fhk_preset_" + p + "_" + i + "_command", "");
                    preset.entries.add(new FastHotkeyEntry(label, cmd, i));
                }
                presets.add(preset);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return presets;
    }

    public void SaveFastHotKeyPresets(List<FastHotkeyPreset> presets, int activeIndex) {
        if (presets == null) presets = new ArrayList<>();
        if (activeIndex < 0 || activeIndex >= presets.size()) activeIndex = 0;
        // Write presets
        FastHotKeyProperties.setProperty("fhk_preset_count", String.valueOf(presets.size()));
        FastHotKeyProperties.setProperty("fhk_active_preset", String.valueOf(activeIndex));
        for (int p = 0; p < presets.size(); p++) {
            FastHotkeyPreset preset = presets.get(p);
            FastHotKeyProperties.setProperty("fhk_preset_" + p + "_name", preset.name);
            FastHotKeyProperties.setProperty("fhk_preset_" + p + "_enabled", Boolean.toString(preset.enabled));
            FastHotKeyProperties.setProperty("fhk_preset_" + p + "_keycode", Integer.toString(preset.keyCode));
            FastHotKeyProperties.setProperty("fhk_preset_" + p + "_length", String.valueOf(preset.entries.size()));
            for (int i = 0; i < preset.entries.size(); i++) {
                FastHotkeyEntry e = preset.entries.get(i);
                FastHotKeyProperties.setProperty("fhk_preset_" + p + "_" + i + "_label", e.label);
                FastHotKeyProperties.setProperty("fhk_preset_" + p + "_" + i + "_command", e.command);
            }
        }
        // Also mirror the active preset into legacy keys for runtime code that still reads them
        if (!presets.isEmpty()) {
            List<FastHotkeyEntry> active = presets.get(activeIndex).entries;
            SaveFastHotKeyEntries(active);
        }
    }

    public int GetActiveFhkPresetIndex(int bounds) {
        try {
            int idx = Integer.parseInt(FastHotKeyProperties.getProperty("fhk_active_preset", "0"));
            if (bounds > 0) idx = Math.max(0, Math.min(idx, bounds - 1));
            return idx;
        } catch (Exception ignored) {
            return 0;
        }
    }

    public int[] GetUIPosition( String Key) {
        String Value = properties.getProperty( Key);
        if (Value == null) return null;
        String[] parts = Value.split(",");
        if (parts.length != 2) return null;
        try {
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            return new int[]{x, y};
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }
   

    private Properties LoadProperties(File file) {
        Properties properties = new Properties();
        try {
            if (file.exists()) {
                properties.load(new java.io.FileReader(file));
            } else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties;
    }


    public void SaveProperties() {
        try {
            AllConfig.INSTANCE.SaveToProperty();
            properties.store(new java.io.FileWriter(CONFIG_FILE), "RatAllOfYou Configurations");
            FastHotKeyProperties.store(new java.io.FileWriter(FastHotKey_FILE), "RatAllOfYou Fast Hotkey Configurations");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
