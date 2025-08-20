package com.aftertime.ratallofyou.UI.config.ConfigData;

import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.UI.config.PropertyRef;

public class FastHotkeyEntry {
    public String label;
    public String command;
    public PropertyRef Labelref;
    public PropertyRef Commandref;
    public FastHotkeyEntry(String label, String command, int index) {
        this.label = label == null ? "" : label;
        this.command = command == null ? "" : command;
        // Use a special ConfigType for FastHotkey fields so generic UI logic can route correctly
        this.Labelref = new PropertyRef(99, index + "_label");
        this.Commandref = new PropertyRef(99, index + "_command");
    }

    /**
     * Persist current entries to the FastHotKey properties
     */
    public void SetProperty()
    {
        ConfigIO.INSTANCE.SaveFastHotKeyEntries(AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES);
    }
    public void RemoveProperty()
    {
        // Caller removes from FAST_HOTKEY_ENTRIES; persist updated list
        ConfigIO.INSTANCE.SaveFastHotKeyEntries(AllConfig.INSTANCE.FAST_HOTKEY_ENTRIES);
    }

}
