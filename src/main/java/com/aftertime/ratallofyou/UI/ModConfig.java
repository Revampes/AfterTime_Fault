package com.aftertime.ratallofyou.UI;

import com.aftertime.ratallofyou.modules.render.NoDebuff;

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
            new ModuleInfo("Pearl Refill (Use at your own risk!)", "Automatically refill ender pearls", "Kuudra", false),
            new ModuleInfo("Pearl Cancel (Use at your own risk!)", "Allow pearl usage when facing floor", "Kuudra", false),
            new ModuleInfo("Crate Beam", "Draw beams on Kuudra supplies (Extremely ugly currently)", "Kuudra", false),
            new ModuleInfo("Crate Highlighter", "Highlight Kuudra crates with ESP", "Kuudra", false),

            // Dungeons
            new ModuleInfo("Invincible Timer", "Show invincibility phase timers", "Dungeons", false),
            new ModuleInfo("Phase 3 Start CountDown", "Timer for phase 3 transitions", "Dungeons", false),
            new ModuleInfo("Phase 3 Tick Timer", "Track instant damage intervals", "Dungeons", false),
            new ModuleInfo("Dungeon Sweat Mode (Use at your own risk)", "Recommend only enable it in f7/m7 (Making new method for DungeonUtils", "Dungeons", false),
            new ModuleInfo("Leap Announce", "Yes announce", "Dungeons", false),
            new ModuleInfo("Key Highlighter", "Box Key (through wall!)", "Dungeons", false),
            new ModuleInfo("Star Mob Highlighter", "Highlights starred mobs and Shadow Assassins (through wall!)", "Dungeons", false),
            new ModuleInfo("Show Secret Clicks", "Highlights when you click on secrets", "Dungeons", false),

            // SkyBlock
            new ModuleInfo("Party Commands", "ehh (not working currently)", "SkyBlock", false),
            new ModuleInfo("Auto Sprint", "Automatically sprint when moving", "SkyBlock", false),

            // Render
            new ModuleInfo("FullBright", "SHINE!", "Render", false),
            new ModuleInfo("No Debuff", "Removes negative effects", "Render", false),

            // GUI
            new ModuleInfo("Move GUI Position", "Enable dragging of UI elements", "GUI", false)
    };

    private static final File configFile = new File("config/ratallofyou.cfg");

    public static Properties loadProperties() {
        Properties props = new Properties();
        if (!configFile.exists()) {
            return props;
        }

        FileInputStream input = null;
        try {
            input = new FileInputStream(configFile);
            props.load(input);
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
        return props;
    }

    public static void loadConfig() {
        Properties props = loadProperties();

        // Load module states
        for (ModuleInfo module : MODULES) {
            String key = module.name.replace(" ", "_").toLowerCase();
            module.enabled = Boolean.parseBoolean(props.getProperty(key + "_enabled", String.valueOf(module.enabled)));
        }

        // Load NoDebuff settings
        NoDebuff.loadConfig(props);
    }

    public static void saveConfig() {
        Properties props = new Properties();

        // Save module states
        for (ModuleInfo module : MODULES) {
            String key = module.name.replace(" ", "_").toLowerCase();
            props.setProperty(key + "_enabled", String.valueOf(module.enabled));
        }

        // Save NoDebuff settings
        props.setProperty("nodebuff_noblindness", String.valueOf(NoDebuff.isNoBlindness()));
        props.setProperty("nodebuff_nofire", String.valueOf(NoDebuff.isNoFire()));
        props.setProperty("nodebuff_clearliquidvision", String.valueOf(NoDebuff.isClearLiquidVision()));

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