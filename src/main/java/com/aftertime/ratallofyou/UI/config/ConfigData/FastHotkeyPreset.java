package com.aftertime.ratallofyou.UI.config.ConfigData;

import java.util.ArrayList;
import java.util.List;

public class FastHotkeyPreset {
    public String name;
    public final List<FastHotkeyEntry> entries = new ArrayList<>();

    public FastHotkeyPreset(String name) {
        this.name = (name == null || name.trim().isEmpty()) ? "Default" : name.trim();
    }
}

