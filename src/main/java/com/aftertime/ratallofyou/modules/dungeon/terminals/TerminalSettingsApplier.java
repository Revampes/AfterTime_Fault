package com.aftertime.ratallofyou.modules.dungeon.terminals;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.BaseConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.config.ModConfig;

import java.awt.*;
import java.util.Map;

/**
 * Applies terminal-related settings from AllConfig to runtime Defaults and per-terminal enable flags.
 */
public final class TerminalSettingsApplier {
    private TerminalSettingsApplier() {}

    public static void applyFromAllConfig() {
        try {
            Class.forName(TerminalGuiCommon.class.getName());
        } catch (Throwable ignored) {}

        // Read from ModConfig instead of AllConfig
        TerminalGuiCommon.Defaults.highPingMode = ModConfig.terminalHighPingMode;
        TerminalGuiCommon.Defaults.timeoutMs = ModConfig.terminalTimeoutMs;
        TerminalGuiCommon.Defaults.firstClickBlockMs = ModConfig.terminalFirstClickMs;
        TerminalGuiCommon.Defaults.scale = ModConfig.terminalScale;
        TerminalGuiCommon.Defaults.offsetX = ModConfig.terminalOffsetX;
        TerminalGuiCommon.Defaults.offsetY = ModConfig.terminalOffsetY;
        TerminalGuiCommon.Defaults.overlayColor = ModConfig.terminalOverlayColor;
        TerminalGuiCommon.Defaults.backgroundColor = ModConfig.terminalBackgroundColor;
        TerminalGuiCommon.Defaults.cornerRadiusBg = ModConfig.terminalCornerRadiusBg;
        TerminalGuiCommon.Defaults.cornerRadiusCell = ModConfig.terminalCornerRadiusCell;
        TerminalGuiCommon.Defaults.queueIntervalMs = ModConfig.terminalHighPingIntervalMs;

        boolean masterOn = ModConfig.enableDungeonTerminals;
        boolean enNumbers = masterOn && ModConfig.terminalEnableNumbers;
        boolean enStarts = masterOn && ModConfig.terminalEnableStartsWith;
        boolean enColors = masterOn && ModConfig.terminalEnableColors;
        boolean enRedGreen = masterOn && ModConfig.terminalEnableRedGreen;
        boolean enRubix = masterOn && ModConfig.terminalEnableRubix;
        boolean enMelody = masterOn && ModConfig.terminalEnableMelody;

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