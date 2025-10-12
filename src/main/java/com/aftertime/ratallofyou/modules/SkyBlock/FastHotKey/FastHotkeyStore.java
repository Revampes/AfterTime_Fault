package com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey;

import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyEntry;
import com.aftertime.ratallofyou.UI.config.ConfigData.FastHotkeyPreset;
import com.aftertime.ratallofyou.UI.newui.config.ModConfigIO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central store for FastHotkey presets backed by ModConfigIO JSON.
 */
public class FastHotkeyStore {
    private static FastHotkeyStore INSTANCE;

    public static FastHotkeyStore getInstance() {
        if (INSTANCE == null) INSTANCE = new FastHotkeyStore();
        return INSTANCE;
    }

    private final List<FastHotkeyPreset> presets = new ArrayList<FastHotkeyPreset>();
    private int activeIndex = 0;

    private FastHotkeyStore() {
        load();
    }

    public synchronized void load() {
        try {
            List<FastHotkeyPreset> loaded = ModConfigIO.loadFhkPresets();
            presets.clear();
            if (loaded != null && !loaded.isEmpty()) {
                presets.addAll(loaded);
                int idx = ModConfigIO.loadFhkActiveIndex();
                activeIndex = Math.max(0, Math.min(idx, Math.max(0, presets.size() - 1)));
            } else {
                // No presets yet -> initialize empty default preset
                FastHotkeyPreset def = new FastHotkeyPreset("Default");
                presets.add(def);
                activeIndex = 0;
                save();
            }
        } catch (Throwable ignored) {}
    }

    public synchronized void save() {
        try {
            ModConfigIO.saveFhkPresets(presets, activeIndex);
        } catch (Throwable ignored) {}
    }

    public synchronized List<FastHotkeyEntry> getActiveEntries() {
        if (presets.isEmpty()) return Collections.emptyList();
        int idx = Math.max(0, Math.min(activeIndex, presets.size() - 1));
        return presets.get(idx).entries;
    }

    public synchronized List<FastHotkeyPreset> getPresetsView() {
        return Collections.unmodifiableList(presets);
    }

    public synchronized int getActiveIndex() {
        return Math.max(0, Math.min(activeIndex, Math.max(0, presets.size() - 1)));
    }

    public synchronized void setActiveIndex(int idx) {
        if (presets.isEmpty()) { activeIndex = 0; return; }
        activeIndex = Math.max(0, Math.min(idx, presets.size() - 1));
        save();
    }

    public synchronized void replaceActiveEntries(List<FastHotkeyEntry> newEntries) {
        if (presets.isEmpty()) return;
        int idx = getActiveIndex();
        presets.get(idx).entries.clear();
        if (newEntries != null) presets.get(idx).entries.addAll(newEntries);
        save();
    }

    public synchronized void setPresets(List<FastHotkeyPreset> newPresets, int newActiveIndex) {
        presets.clear();
        if (newPresets != null) presets.addAll(newPresets);
        activeIndex = Math.max(0, Math.min(newActiveIndex, Math.max(0, presets.size() - 1)));
        save();
    }

    // Convenience mutators
    public synchronized int addPreset(String name) {
        if (name == null || name.trim().isEmpty()) name = "Preset " + (presets.size() + 1);
        FastHotkeyPreset p = new FastHotkeyPreset(name.trim());
        presets.add(p);
        activeIndex = presets.size() - 1;
        save();
        return activeIndex;
    }

    public synchronized void removePreset(int index) {
        if (index < 0 || index >= presets.size()) return;
        presets.remove(index);
        if (presets.isEmpty()) {
            presets.add(new FastHotkeyPreset("Default"));
        }
        if (activeIndex >= presets.size()) activeIndex = presets.size() - 1;
        save();
    }

    public synchronized void setPresetKey(int index, int keyCode) {
        if (index < 0 || index >= presets.size()) return;
        presets.get(index).keyCode = keyCode;
        save();
    }

    public synchronized void setPresetEnabled(int index, boolean enabled) {
        if (index < 0 || index >= presets.size()) return;
        presets.get(index).enabled = enabled;
        save();
    }
}
