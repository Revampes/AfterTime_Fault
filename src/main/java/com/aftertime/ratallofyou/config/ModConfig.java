package com.aftertime.ratallofyou.config;

import java.io.*;
import java.util.Properties;

public class ModConfig {
    // Module metadata storage
    public static class ModuleInfo {
        public final String name;
        public final String description;
        public final String category;
        public boolean enabled;
        public float sliderValue; // For position adjustments

        public ModuleInfo(String name, String description, String category, boolean defaultState) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.enabled = defaultState;
            this.sliderValue = 0.5f; // Default slider position
        }

        public boolean hasSlider() {
            return false; // Override in specific modules that need sliders
        }
    }

    // Module definitions
    public static final ModuleInfo[] MODULES = {
            // Kuudra
            new ModuleInfo("Pearl Refill", "Automatically refill ender pearls", "Kuudra", false),
            new ModuleInfo("Pearl Cancel", "Allow pearl usage when facing floor", "Kuudra", false),

            // Dungeons
            new ModuleInfo("Invincible Timer", "Show invincibility phase timers", "Dungeons", false),
            new ModuleInfo("Phase 3 Timer", "Timer for phase 3 transitions", "Dungeons", false),
            new ModuleInfo("Phase 3 Tick Timer", "Track instant damage intervals", "Dungeons", false) {
                @Override
                public boolean hasSlider() { return true; }
            },

            // SkyBlock
            new ModuleInfo("Auto Sprint", "Automatically sprint when moving", "SkyBlock", false)
    };

    private static final File configFile = new File("config/ratallofyou.cfg");

    public static void loadConfig() {
        if (!configFile.exists()) {
            saveConfig();
            return;
        }

        Properties props = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(configFile);
            props.load(input);

            for (ModuleInfo module : MODULES) {
                String key = module.name.replace(" ", "_").toLowerCase();
                module.enabled = Boolean.parseBoolean(props.getProperty(key + "_enabled", String.valueOf(module.enabled)));
                module.sliderValue = Float.parseFloat(props.getProperty(key + "_slider", String.valueOf(module.sliderValue)));
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
            props.setProperty(key + "_slider", String.valueOf(module.sliderValue));
        }

        OutputStream output = null;
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