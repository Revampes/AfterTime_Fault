package com.aftertime.ratallofyou.UI;

import java.io.*;
import java.util.Properties;

public class ModConfig {
    // Module metadata storage
    public static class ModuleInfo {
        public final String name;
        public final String description;
        public final String category;
        public boolean enabled;

        public ModuleInfo(String name, String description, String category, boolean defaultState) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.enabled = defaultState;
        }
    }

    public static final ModuleInfo[] MODULES = {
            // Kuudra
            new ModuleInfo("Pearl Refill(Use at your own risk!)", "Automatically refill ender pearls", "Kuudra", false),
            new ModuleInfo("Pearl Cancel(Use at your own risk!)", "Allow pearl usage when facing floor", "Kuudra", false),

            // Dungeons
            new ModuleInfo("Invincible Timer", "Show invincibility phase timers", "Dungeons", true),
            new ModuleInfo("Phase 3 Start CountDown", "Timer for phase 3 transitions", "Dungeons", false),
            new ModuleInfo("Phase 3 Tick Timer", "Track instant damage intervals", "Dungeons", false),
            new ModuleInfo("Dungeon Sweat Mode (Use at your own risk)", "Recommend only enable it in f7/m7", "Dungeons", false),
            new ModuleInfo("Leap Announce", "Yes announce", "Dungeons", false),
            new ModuleInfo("Key Highlighter", "Box Key (through wall)", "Dungeons", false),
            new ModuleInfo("Star Mob Highlighter", "Highlights starred mobs and Shadow Assassins", "Dungeons", false),

            // SkyBlock
            new ModuleInfo("Auto Sprint", "Automatically sprint when moving", "SkyBlock", true),

            // GUI
            new ModuleInfo("Move GUI Position", "Enable dragging of UI elements", "GUI", false)
    };

    private static final File configFile = new File("config/ratallofyou.cfg");

    public static void loadConfig() {
        if (!configFile.exists()) {
            saveConfig();
            return;
        }

        Properties props = new Properties();
        FileInputStream input = null;
        try {
            input = new FileInputStream(configFile);
            props.load(input);

            for (ModuleInfo module : MODULES) {
                String key = module.name.replace(" ", "_").toLowerCase();
                module.enabled = Boolean.parseBoolean(props.getProperty(key + "_enabled", String.valueOf(module.enabled)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveConfig() {
        Properties props = new Properties();

        for (ModuleInfo module : MODULES) {
            String key = module.name.replace(" ", "_").toLowerCase();
            props.setProperty(key + "_enabled", String.valueOf(module.enabled));
        }

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(configFile);
            props.store(output, "Rat All Of You Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}