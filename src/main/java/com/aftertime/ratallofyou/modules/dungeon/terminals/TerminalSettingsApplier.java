package com.aftertime.ratallofyou.modules.dungeon.terminals;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;

import java.awt.*;
import java.util.Map;

/**
 * Applies terminal-related settings from AllConfig to runtime Defaults and per-terminal enable flags.
 */
public final class TerminalSettingsApplier {
    private TerminalSettingsApplier() {}

    public static void applyFromAllConfig() {
        try {
            // Ensure common class is initialized (it force-loads terminal classes)
            Class.forName(TerminalGuiCommon.class.getName());
        } catch (Throwable ignored) {}

        // Pull terminal group settings
        Map<String, BaseConfig<?>> term = AllConfig.INSTANCE.TERMINAL_CONFIGS;

        // Defaults mapping
        TerminalGuiCommon.Defaults.highPingMode = getBool(term, "terminal_high_ping_mode", TerminalGuiCommon.Defaults.highPingMode);
//        TerminalGuiCommon.Defaults.phoenixClientCompat = getBool(term, "terminal_phoenix_client_compat", TerminalGuiCommon.Defaults.phoenixClientCompat);
        TerminalGuiCommon.Defaults.timeoutMs = getInt(term, "terminal_timeout_ms", TerminalGuiCommon.Defaults.timeoutMs);
        TerminalGuiCommon.Defaults.firstClickBlockMs = getInt(term, "terminal_first_click_ms", TerminalGuiCommon.Defaults.firstClickBlockMs);
        TerminalGuiCommon.Defaults.scale = getFloat(term, "terminal_scale", TerminalGuiCommon.Defaults.scale);
        TerminalGuiCommon.Defaults.offsetX = getInt(term, "terminal_offset_x", TerminalGuiCommon.Defaults.offsetX);
        TerminalGuiCommon.Defaults.offsetY = getInt(term, "terminal_offset_y", TerminalGuiCommon.Defaults.offsetY);
        Color overlay = getColor(term, "terminal_overlay_color", new Color(TerminalGuiCommon.Defaults.overlayColor, true));
        TerminalGuiCommon.Defaults.overlayColor = overlay.getRGB();
        Color bg = getColor(term, "terminal_background_color", new Color(TerminalGuiCommon.Defaults.backgroundColor, true));
        TerminalGuiCommon.Defaults.backgroundColor = bg.getRGB();
        // Optional rounded corner radii (pixels)
        TerminalGuiCommon.Defaults.cornerRadiusBg = getInt(term, "terminal_corner_radius_bg", TerminalGuiCommon.Defaults.cornerRadiusBg);
        TerminalGuiCommon.Defaults.cornerRadiusCell = getInt(term, "terminal_corner_radius_cell", TerminalGuiCommon.Defaults.cornerRadiusCell);
        // High ping pacing interval (ms)
        TerminalGuiCommon.Defaults.queueIntervalMs = getInt(term, "terminal_high_ping_interval_ms", TerminalGuiCommon.Defaults.queueIntervalMs);

        // Master module toggle: if disabled, force-disable all terminal helpers
        boolean masterOn = true;
        BaseConfig<?> moduleCfg = AllConfig.INSTANCE.MODULES.get("dungeons_terminals");
        if (moduleCfg instanceof ModuleInfo) {
            Object v = moduleCfg.Data;
            if (v instanceof Boolean) masterOn = (Boolean) v;
        }

        // Per-terminal enables (guarded by master)
        boolean enNumbers = masterOn && getBool(term, "terminal_enable_numbers", true);
        boolean enStarts = masterOn && getBool(term, "terminal_enable_starts_with", true);
        boolean enColors = masterOn && getBool(term, "terminal_enable_colors", true);
        boolean enRedGreen = masterOn && getBool(term, "terminal_enable_red_green", true);
        boolean enRubix = masterOn && getBool(term, "terminal_enable_rubix", true);
        boolean enMelody = masterOn && getBool(term, "terminal_enable_melody", true);

        // Apply to runtime listeners
        try { numbers.setEnabled(enNumbers); } catch (Throwable ignored) {}
        try { startswith.setEnabled(enStarts); } catch (Throwable ignored) {}
        try { Colors.setEnabled(enColors); } catch (Throwable ignored) {}
        try { redgreen.setEnabled(enRedGreen); } catch (Throwable ignored) {}
        try { rubix.setEnabled(enRubix); } catch (Throwable ignored) {}
        try { melody.setEnabled(enMelody); } catch (Throwable ignored) {}
    }

    private static boolean getBool(Map<String, BaseConfig<?>> map, String key, boolean def) {
        BaseConfig<?> c = map.get(key);
        return c != null && c.Data instanceof Boolean ? (Boolean) c.Data : def;
    }
    private static int getInt(Map<String, BaseConfig<?>> map, String key, int def) {
        BaseConfig<?> c = map.get(key);
        return c != null && c.Data instanceof Integer ? (Integer) c.Data : def;
    }
    private static float getFloat(Map<String, BaseConfig<?>> map, String key, float def) {
        BaseConfig<?> c = map.get(key);
        return c != null && c.Data instanceof Float ? (Float) c.Data : def;
    }
    private static Color getColor(Map<String, BaseConfig<?>> map, String key, Color def) {
        BaseConfig<?> c = map.get(key);
        return c != null && c.Data instanceof Color ? (Color) c.Data : def;
    }
}