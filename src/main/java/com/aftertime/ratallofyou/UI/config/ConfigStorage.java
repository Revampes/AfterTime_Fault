package com.aftertime.ratallofyou.UI.config;

import com.aftertime.ratallofyou.UI.UIDragger;
import com.aftertime.ratallofyou.modules.render.EtherwarpOverlay;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ConfigStorage {
    private static final File MAIN_CONFIG_FILE = new File("config/ratallofyou.cfg");
    private static final File POSITIONS_CONFIG_FILE = new File("config/ratallofyou_positions.cfg");
    private static final File COMMANDS_CONFIG_FILE = new File("config/ratallofyou_commands.cfg");
    private static final File NODEBUFF_CONFIG_FILE = new File("config/ratallofyou_nodebuff.cfg");

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
            new ModuleInfo("Crate Beam", "Draw beams on Kuudra supplies", "Kuudra", false),
            new ModuleInfo("Crate Highlighter", "Box Kuudra crates", "Kuudra", false),
            new ModuleInfo("Pearl Lineups", "Show pearl aim spots", "Kuudra", false),

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
            new ModuleInfo("Party Commands", "Only work in party chat", "SkyBlock", false),
            new ModuleInfo("Auto Sprint", "Automatically sprint when moving", "SkyBlock", false),

            // Render
            new ModuleInfo("FullBright", "SHINE!", "Render", false),
            new ModuleInfo("No Debuff", "Removes negative effects", "Render", false),
            new ModuleInfo("Etherwarp Overlay", "Shows where you'll teleport with etherwarp", "Render", false),

            // GUI
            new ModuleInfo("Move GUI Position", "Enable dragging of UI elements", "GUI", false)
    };

    // Add these inside the ConfigStorage class
    public static class CommandConfig {
        public final String name;
        public final String description;
        public boolean enabled;

        public CommandConfig(String name, String description, boolean defaultState) {
            this.name = name;
            this.description = description;
            this.enabled = defaultState;
        }
    }

    public static class NoDebuffConfig {
        public final String name;
        public final String description;
        public boolean enabled;

        public NoDebuffConfig(String name, String description, boolean defaultState) {
            this.name = name;
            this.description = description;
            this.enabled = defaultState;
        }
    }

    public static class EtherwarpConfig {
        public final String name;
        public final String description;
        public boolean enabled;

        public EtherwarpConfig(String name, String description, boolean defaultState) {
            this.name = name;
            this.description = description;
            this.enabled = defaultState;
        }
    }

    // Command configurations
    private static final List<CommandConfig> COMMAND_CONFIGS = Arrays.asList(
            new CommandConfig("Warp", "Enable !warp command", false),
            new CommandConfig("Warp Transfer", "Enable !warptransfer command", false),
            new CommandConfig("Coords", "Enable !coords command", false),
            new CommandConfig("All Invite", "Enable !allinvite command", false),
            new CommandConfig("Boop", "Enable !boop command", false),
            new CommandConfig("Coin Flip", "Enable !cf command", false),
            new CommandConfig("8Ball", "Enable !8ball command", false),
            new CommandConfig("Dice", "Enable !dice command", false),
            new CommandConfig("Party Transfer", "Enable !pt command", false),
            new CommandConfig("TPS", "Enable !tps command", false),
            new CommandConfig("Downtime", "Enable !dt command", false),
            new CommandConfig("Queue Instance", "Enable dungeon queue commands", false),
            new CommandConfig("Demote", "Enable !demote command", false),
            new CommandConfig("Promote", "Enable !promote command", false),
            new CommandConfig("Disband", "Enable !disband command", false),
            new CommandConfig("pt+warp", "Enable !ptwarp command", false)
    );

    // NoDebuff configurations
    private static final List<NoDebuffConfig> NODEBUFF_CONFIGS = Arrays.asList(
            new NoDebuffConfig("Remove Fire Overlay", "Disables the fire overlay effect", false),
            new NoDebuffConfig("Ignore Blindness", "Removes blindness effect", false),
            new NoDebuffConfig("Clear Liquid Vision", "Clears water/lava fog", false)
    );

    private static final List<EtherwarpConfig> ETHERWARP_CONFIGS = Arrays.asList(
            new EtherwarpConfig("Sync with server", "Sync etherwarp with server position", false),
            new EtherwarpConfig("Only show when sneak", "Only show overlay when sneaking", true),
            new EtherwarpConfig("Show fail location", "Show where etherwarp would fail", true),
            new EtherwarpConfig("Render Method", "Select how to render the overlay", true)
    );

    public static Properties loadProperties(File file) {
        Properties props = new Properties();
        if (!file.exists()) {
            return props;
        }

        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
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

    public static void saveProperties(Properties props, File file, String comments) {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            props.store(output, comments);
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

    public static void loadAllConfigs() {
        loadMainConfig();
        loadPositionsConfig();
        loadCommandsConfig();
        loadNoDebuffConfig();
    }

    public static void loadMainConfig() {
        Properties props = loadProperties(MAIN_CONFIG_FILE);
        for (ModuleInfo module : MODULES) {
            String key = module.name.replace(" ", "_").toLowerCase();
            module.enabled = Boolean.parseBoolean(props.getProperty(key + "_enabled", String.valueOf(module.enabled)));
        }
    }

    public static void saveMainConfig() {
        Properties props = new Properties();
        for (ModuleInfo module : MODULES) {
            String key = module.name.replace(" ", "_").toLowerCase();
            props.setProperty(key + "_enabled", String.valueOf(module.enabled));
        }
        saveProperties(props, MAIN_CONFIG_FILE, "Rat All Of You Main Configuration");
    }

    public static void loadPositionsConfig() {
        Properties props = loadProperties(POSITIONS_CONFIG_FILE);
        for (Map.Entry<String, UIDragger.UIPosition> entry : UIDragger.getInstance().getAllElements().entrySet()) {
            String key = entry.getKey().replace(" ", "_").toLowerCase();
            try {
                int x = Integer.parseInt(props.getProperty(key + "_x", String.valueOf(entry.getValue().x)));
                int y = Integer.parseInt(props.getProperty(key + "_y", String.valueOf(entry.getValue().y)));
                UIDragger.getInstance().registerElement(entry.getKey(), x, y);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public static void savePositionsConfig() {
        Properties props = new Properties();
        for (Map.Entry<String, UIDragger.UIPosition> entry : UIDragger.getInstance().getAllElements().entrySet()) {
            String key = entry.getKey().replace(" ", "_").toLowerCase();
            props.setProperty(key + "_x", String.valueOf(entry.getValue().x));
            props.setProperty(key + "_y", String.valueOf(entry.getValue().y));
        }
        saveProperties(props, POSITIONS_CONFIG_FILE, "UI Positions Configuration");
    }

    public static void loadCommandsConfig() {
        Properties props = loadProperties(COMMANDS_CONFIG_FILE);
        for (CommandConfig config : COMMAND_CONFIGS) {
            String key = config.name.toLowerCase().replace(" ", "_");
            config.enabled = Boolean.parseBoolean(props.getProperty(key, String.valueOf(config.enabled)));
        }
    }

    public static void saveCommandsConfig() {
        Properties props = new Properties();
        for (CommandConfig config : COMMAND_CONFIGS) {
            String key = config.name.toLowerCase().replace(" ", "_");
            props.setProperty(key, String.valueOf(config.enabled));
        }
        saveProperties(props, COMMANDS_CONFIG_FILE, "Command Settings Configuration");
    }

    public static List<String> getUniqueCategories() {
        List<String> categories = new ArrayList<String>();
        for (ModuleInfo module : MODULES) {
            if (!categories.contains(module.category)) {
                categories.add(module.category);
            }
        }
        return categories;
    }

    public static List<ModuleInfo> getModulesByCategory(String category) {
        List<ModuleInfo> filteredModules = new ArrayList<ModuleInfo>();
        for (ModuleInfo module : MODULES) {
            if (module.category.equals(category)) {
                filteredModules.add(module);
            }
        }
        return filteredModules;
    }

    public static List<CommandConfig> getCommandConfigs() {
        return Collections.unmodifiableList(COMMAND_CONFIGS);
    }

    public static List<NoDebuffConfig> getNoDebuffConfigs() {
        return Collections.unmodifiableList(NODEBUFF_CONFIGS);
    }

    public static void loadNoDebuffConfig() {
        Properties props = loadProperties(NODEBUFF_CONFIG_FILE);
        for (NoDebuffConfig config : NODEBUFF_CONFIGS) {
            String key = config.name.toLowerCase().replace(" ", "_");
            config.enabled = Boolean.parseBoolean(props.getProperty(key, String.valueOf(config.enabled)));
        }
    }

    public static void saveNoDebuffConfig() {
        Properties props = new Properties();
        for (NoDebuffConfig config : NODEBUFF_CONFIGS) {
            String key = config.name.toLowerCase().replace(" ", "_");
            props.setProperty(key, String.valueOf(config.enabled));
        }
        saveProperties(props, NODEBUFF_CONFIG_FILE, "NoDebuff Settings Configuration");
    }

    public static List<EtherwarpConfig> getEtherwarpConfigs() {
        return Collections.unmodifiableList(ETHERWARP_CONFIGS);
    }

    public static void loadEtherwarpConfig() {
        Properties props = loadProperties(new File("config/ratallofyou_etherwarp.cfg"));
        for (EtherwarpConfig config : ETHERWARP_CONFIGS) {
            String key = config.name.toLowerCase().replace(" ", "_");
            config.enabled = Boolean.parseBoolean(props.getProperty(key, String.valueOf(config.enabled)));
        }

        // Load color values
        String colorStr = props.getProperty("overlay_color", "0,255,0,150");
        String[] colorParts = colorStr.split(",");
        if (colorParts.length == 4) {
            EtherwarpOverlay.etherwarpOverlayColor = new Color(
                    Integer.parseInt(colorParts[0]),
                    Integer.parseInt(colorParts[1]),
                    Integer.parseInt(colorParts[2]),
                    Integer.parseInt(colorParts[3])
            );
        }

        colorStr = props.getProperty("fail_color", "255,0,0,150");
        colorParts = colorStr.split(",");
        if (colorParts.length == 4) {
            EtherwarpOverlay.etherwarpOverlayFailColor = new Color(
                    Integer.parseInt(colorParts[0]),
                    Integer.parseInt(colorParts[1]),
                    Integer.parseInt(colorParts[2]),
                    Integer.parseInt(colorParts[3])
            );
        }
    }

    public static void saveEtherwarpConfig() {
        Properties props = new Properties();
        for (EtherwarpConfig config : ETHERWARP_CONFIGS) {
            String key = config.name.toLowerCase().replace(" ", "_");
            props.setProperty(key, String.valueOf(config.enabled));
        }

        // Save color values
        props.setProperty("overlay_color",
                EtherwarpOverlay.etherwarpOverlayColor.getRed() + "," +
                        EtherwarpOverlay.etherwarpOverlayColor.getGreen() + "," +
                        EtherwarpOverlay.etherwarpOverlayColor.getBlue() + "," +
                        EtherwarpOverlay.etherwarpOverlayColor.getAlpha());

        props.setProperty("fail_color",
                EtherwarpOverlay.etherwarpOverlayFailColor.getRed() + "," +
                        EtherwarpOverlay.etherwarpOverlayFailColor.getGreen() + "," +
                        EtherwarpOverlay.etherwarpOverlayFailColor.getBlue() + "," +
                        EtherwarpOverlay.etherwarpOverlayFailColor.getAlpha());

        saveProperties(props, new File("config/ratallofyou_etherwarp.cfg"), "Etherwarp Overlay Configuration");
    }
}