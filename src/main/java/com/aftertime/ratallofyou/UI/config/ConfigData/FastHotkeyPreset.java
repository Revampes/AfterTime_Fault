package com.aftertime.ratallofyou.UI.config.ConfigData;

import java.util.ArrayList;
import java.util.List;

public class FastHotkeyPreset {
    public String name;
    public final List<FastHotkeyEntry> entries = new ArrayList<>();
    // New: per-preset toggle and keybind
    public boolean enabled = false;
    public int keyCode = 0; // LWJGL Keyboard key code; 0 = unbound

    public FastHotkeyPreset(String name) {
        this.name = (name == null || name.trim().isEmpty()) ? "Default" : name.trim();
    }
}
