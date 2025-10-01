package com.aftertime.ratallofyou.UI.config;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.Map;
import com.aftertime.ratallofyou.UI.config.ConfigData.*;
import com.aftertime.ratallofyou.UI.config.OptionElements.*;

public class SubSettingAdders {
    public static void addSubSettingNameTag(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.NAMETAG_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 13);
        }
    }

    public static void addSubSettingPlayerESP(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.PLAYERESP_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 12);
        }
    }

    public static void addSubSettingCustomCape(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.CUSTOMCAPE_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 14);
        }
    }

    public static void addSubSettingDarkMode(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.DARKMODE_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 15);
        }
    }

    public static void addSubSettingChestOpen(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.KUUDRA_CHESTOPEN_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 7);
        }
    }

    public static void addSubSettingHotbarSwap(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.HOTBARSWAP_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 8);
        }
    }

    public static void addSubSettingAutoFish(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.AUTOFISH_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 10);
        }
    }

    public static void addSubSettingAutoExperiment(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.AUTOEXPERIMENT_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 11);
        }
    }

    public static void addSubSettingTerminal(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.TERMINAL_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 4);
        }
    }

    public static void addSubSettingFastHotkey(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.FASTHOTKEY_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 6);
        }
    }

    public static void addSubSettingCommand(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.COMMAND_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 0);
        }
    }

    public static void addSubSettingNoDebuff(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.NODEBUFF_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 2);
        }
    }

    public static void addSubSettingEtherwarp(ModSettingsGui gui, Integer y) {
        for (Map.Entry<String, BaseConfig<?>> e : AllConfig.INSTANCE.ETHERWARP_CONFIGS.entrySet()) {
            addEntryAsOption(gui, e, y, 3);
        }
    }

    private static void addEntryAsOption(ModSettingsGui gui, Map.Entry<String, BaseConfig<?>> entry, Integer y, int ConfigType) {
        gui.AddEntryAsOption(entry, y, ConfigType);
    }
}

