package com.aftertime.ratallofyou.UI.config;

import com.aftertime.ratallofyou.UI.UIDragger;
import com.aftertime.ratallofyou.modules.dungeon.terminals.startswith;
import com.aftertime.ratallofyou.modules.render.EtherwarpOverlay;
import com.aftertime.ratallofyou.modules.render.NoDebuff;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

public class ConfigStorage {
    private static final File MAIN_CONFIG_FILE = new File("config/ratallofyou.cfg");
    private static final File POSITIONS_CONFIG_FILE = new File("config/ratallofyou_positions.cfg");
    private static final File COMMANDS_CONFIG_FILE = new File("config/ratallofyou_commands.cfg");
    private static final File NODEBUFF_CONFIG_FILE = new File("config/ratallofyou_nodebuff.cfg");
    // Added dedicated Fast Hotkey storage file
    private static final File FASTHOTKEY_CONFIG_FILE = new File("config/ratallofyou_fast_hotkey.cfg");
    // New: Terminals settings file
    private static final File TERMINALS_CONFIG_FILE = new File("config/ratallofyou_terminals.cfg");

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
            // New: Dungeon Terminals helper (custom GUI)
            new ModuleInfo("Dungeon Terminals", "Custom GUI and helpers for SkyBlock dungeon terminals", "Dungeons", false),

            // SkyBlock
            new ModuleInfo("Party Commands", "Only work in party chat", "SkyBlock", false),
            new ModuleInfo("Auto Sprint", "Automatically sprint when moving", "SkyBlock", false),
            new ModuleInfo("Fast Hotkey", "Fast hotkey switching", "SkyBlock", false),

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

    // New: Terminal settings container
    public static class TerminalSettings {
        public boolean highPingMode = false;
        public boolean phoenixClientCompat = false;
        public float scale = 1.0f;
        public int timeoutMs = 500;
        public int firstClickMs = 0;
        public int offsetX = 0;
        public int offsetY = 0;
        public Color overlayColor = new Color(0, 255, 0, 255);
        public Color backgroundColor = new Color(0, 0, 0, 127);
        // Per-terminal enable toggles
        public boolean enableStartsWith = true;
        public boolean enableColors = true;
    }

    // New Fast Hotkey entry model
    public static class FastHotKeyEntry {
        public String label;
        public String command;
        public FastHotKeyEntry(String label, String command) {
            this.label = label == null ? "" : label;
            this.command = command == null ? "" : command;
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

    // Fast Hotkey entries (max 12)
    private static final List<FastHotKeyEntry> FAST_HOTKEY_ENTRIES = new ArrayList<FastHotKeyEntry>();

    // Terminals settings singleton
    private static final TerminalSettings TERMINAL_SETTINGS = new TerminalSettings();

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
        // Load Fast Hotkey definitions
        loadFastHotKeyConfig();
        // Load Etherwarp settings (toggles, colors, render method)
        loadEtherwarpConfig();
        // Load Terminal helper settings
        loadTerminalConfig();

        // Apply NoDebuff settings to runtime on startup
        for (ModuleInfo module : MODULES) {
            if ("No Debuff".equals(module.name)) {
                NoDebuff.setEnabled(module.enabled);
                boolean fire = false, blind = false, liquid = false;
                for (NoDebuffConfig cfg : NODEBUFF_CONFIGS) {
                    if ("Remove Fire Overlay".equals(cfg.name)) fire = cfg.enabled;
                    else if ("Ignore Blindness".equals(cfg.name)) blind = cfg.enabled;
                    else if ("Clear Liquid Vision".equals(cfg.name)) liquid = cfg.enabled;
                }
                NoDebuff.setNoFire(fire);
                NoDebuff.setNoBlindness(blind);
                NoDebuff.setClearLiquidVision(liquid);
                break;
            }
        }

        // Apply Terminal settings to runtime (module enable handled elsewhere)
//        applyTerminalSettingsToRuntime();
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

        // Apply toggles to runtime booleans
        if (!ETHERWARP_CONFIGS.isEmpty()) {
            // Indexes: 0 Sync, 1 OnlySneak, 2 ShowFail, 3 Render Method (bool placeholder)
            EtherwarpOverlay.etherwarpSyncWithServer = ETHERWARP_CONFIGS.get(0).enabled;
            EtherwarpOverlay.etherwarpOverlayOnlySneak = ETHERWARP_CONFIGS.get(1).enabled;
            EtherwarpOverlay.etherwarpShowFailLocation = ETHERWARP_CONFIGS.get(2).enabled;
        }

        // Load render method (0=Edges,1=Filled,2=Both) and map to highlightType (0,2,4)
        int defaultMethod = Math.max(0, Math.min(2, EtherwarpOverlay.etherwarpHighlightType / 2));
        int methodIdx;
        try {
            methodIdx = Integer.parseInt(props.getProperty("render_method", String.valueOf(defaultMethod)));
        } catch (NumberFormatException e) {
            methodIdx = defaultMethod;
        }
        methodIdx = Math.max(0, Math.min(2, methodIdx));
        EtherwarpOverlay.etherwarpHighlightType = methodIdx * 2;
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

        // Save render method index (not the GL type)
        props.setProperty("render_method", String.valueOf(Math.max(0, Math.min(2, EtherwarpOverlay.etherwarpHighlightType / 2))));

        saveProperties(props, new File("config/ratallofyou_etherwarp.cfg"), "Etherwarp Overlay Configuration");
    }

    // ============================
    // Fast Hotkey config
    // ============================
    public static List<FastHotKeyEntry> getFastHotKeyEntries() {
        return FAST_HOTKEY_ENTRIES; // intentionally modifiable
    }

    public static void loadFastHotKeyConfig() {
        FAST_HOTKEY_ENTRIES.clear();
        Properties props = loadProperties(FASTHOTKEY_CONFIG_FILE);
        int count = 0;
        try {
            count = Integer.parseInt(props.getProperty("count", "0"));
        } catch (NumberFormatException ignored) {}
        count = Math.max(0, Math.min(12, count));
        for (int i = 0; i < count; i++) {
            String label = props.getProperty("label_" + i, "");
            String command = props.getProperty("command_" + i, "");
            FAST_HOTKEY_ENTRIES.add(new FastHotKeyEntry(label, command));
        }
    }

    public static void saveFastHotKeyConfig() {
        Properties props = new Properties();
        int count = Math.min(12, FAST_HOTKEY_ENTRIES.size());
        props.setProperty("count", String.valueOf(count));
        for (int i = 0; i < count; i++) {
            FastHotKeyEntry e = FAST_HOTKEY_ENTRIES.get(i);
            props.setProperty("label_" + i, e.label == null ? "" : e.label);
            props.setProperty("command_" + i, e.command == null ? "" : e.command);
        }
        saveProperties(props, FASTHOTKEY_CONFIG_FILE, "Fast Hotkey Entries");
    }

    // ============================
    // Terminals config
    // ============================
    public static TerminalSettings getTerminalSettings() { return TERMINAL_SETTINGS; }

    public static void loadTerminalConfig() {
        Properties props = loadProperties(TERMINALS_CONFIG_FILE);
        TERMINAL_SETTINGS.highPingMode = Boolean.parseBoolean(props.getProperty("high_ping_mode", String.valueOf(TERMINAL_SETTINGS.highPingMode)));
        TERMINAL_SETTINGS.phoenixClientCompat = Boolean.parseBoolean(props.getProperty("phoenix_client", String.valueOf(TERMINAL_SETTINGS.phoenixClientCompat)));
        try { TERMINAL_SETTINGS.scale = Float.parseFloat(props.getProperty("scale", String.valueOf(TERMINAL_SETTINGS.scale))); } catch (Exception ignored) {}
        try { TERMINAL_SETTINGS.timeoutMs = Integer.parseInt(props.getProperty("timeout_ms", String.valueOf(TERMINAL_SETTINGS.timeoutMs))); } catch (Exception ignored) {}
        try { TERMINAL_SETTINGS.firstClickMs = Integer.parseInt(props.getProperty("first_click_ms", String.valueOf(TERMINAL_SETTINGS.firstClickMs))); } catch (Exception ignored) {}
        try { TERMINAL_SETTINGS.offsetX = Integer.parseInt(props.getProperty("offset_x", String.valueOf(TERMINAL_SETTINGS.offsetX))); } catch (Exception ignored) {}
        try { TERMINAL_SETTINGS.offsetY = Integer.parseInt(props.getProperty("offset_y", String.valueOf(TERMINAL_SETTINGS.offsetY))); } catch (Exception ignored) {}
        // Per-terminal toggles
        TERMINAL_SETTINGS.enableStartsWith = Boolean.parseBoolean(props.getProperty("enable_startswith", String.valueOf(TERMINAL_SETTINGS.enableStartsWith)));
        TERMINAL_SETTINGS.enableColors = Boolean.parseBoolean(props.getProperty("enable_colors", String.valueOf(TERMINAL_SETTINGS.enableColors)));

        String oc = props.getProperty("overlay_color", "0,255,0,255");
        String[] p = oc.split(",");
        if (p.length == 4) {
            try {
                TERMINAL_SETTINGS.overlayColor = new Color(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
            } catch (Exception ignored) {}
        }
        String bc = props.getProperty("background_color", "0,0,0,127");
        p = bc.split(",");
        if (p.length == 4) {
            try {
                TERMINAL_SETTINGS.backgroundColor = new Color(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
            } catch (Exception ignored) {}
        }
    }

    public static void saveTerminalConfig() {
        Properties props = new Properties();
        props.setProperty("high_ping_mode", String.valueOf(TERMINAL_SETTINGS.highPingMode));
        props.setProperty("phoenix_client", String.valueOf(TERMINAL_SETTINGS.phoenixClientCompat));
        props.setProperty("scale", String.valueOf(TERMINAL_SETTINGS.scale));
        props.setProperty("timeout_ms", String.valueOf(TERMINAL_SETTINGS.timeoutMs));
        props.setProperty("first_click_ms", String.valueOf(TERMINAL_SETTINGS.firstClickMs));
        props.setProperty("offset_x", String.valueOf(TERMINAL_SETTINGS.offsetX));
        props.setProperty("offset_y", String.valueOf(TERMINAL_SETTINGS.offsetY));
        // Per-terminal toggles
        props.setProperty("enable_startswith", String.valueOf(TERMINAL_SETTINGS.enableStartsWith));
        props.setProperty("enable_colors", String.valueOf(TERMINAL_SETTINGS.enableColors));
        props.setProperty("overlay_color",
                TERMINAL_SETTINGS.overlayColor.getRed() + "," + TERMINAL_SETTINGS.overlayColor.getGreen() + "," + TERMINAL_SETTINGS.overlayColor.getBlue() + "," + TERMINAL_SETTINGS.overlayColor.getAlpha());
        props.setProperty("background_color",
                TERMINAL_SETTINGS.backgroundColor.getRed() + "," + TERMINAL_SETTINGS.backgroundColor.getGreen() + "," + TERMINAL_SETTINGS.backgroundColor.getBlue() + "," + TERMINAL_SETTINGS.backgroundColor.getAlpha());
        saveProperties(props, TERMINALS_CONFIG_FILE, "Dungeon Terminals Settings");
    }

//    public static void applyTerminalSettingsToRuntime() {
//        startswith.setHighPingMode(TERMINAL_SETTINGS.highPingMode);
//        startswith.setPhoenixClientCompat(TERMINAL_SETTINGS.phoenixClientCompat);
//        startswith.setScale(TERMINAL_SETTINGS.scale);
//        startswith.setTimeoutMs(TERMINAL_SETTINGS.timeoutMs);
//        startswith.setFirstClickBlockMs(TERMINAL_SETTINGS.firstClickMs);
//        startswith.setOffsetX(TERMINAL_SETTINGS.offsetX);
//        startswith.setOffsetY(TERMINAL_SETTINGS.offsetY);
//        startswith.setOverlayColor(new java.awt.Color(
//                TERMINAL_SETTINGS.overlayColor.getRed(),
//                TERMINAL_SETTINGS.overlayColor.getGreen(),
//                TERMINAL_SETTINGS.overlayColor.getBlue(),
//                TERMINAL_SETTINGS.overlayColor.getAlpha()
//        ).getRGB());
//        startswith.setBackgroundColor(new java.awt.Color(
//                TERMINAL_SETTINGS.backgroundColor.getRed(),
//                TERMINAL_SETTINGS.backgroundColor.getGreen(),
//                TERMINAL_SETTINGS.backgroundColor.getBlue(),
//                TERMINAL_SETTINGS.backgroundColor.getAlpha()
//        ).getRGB());
//        // Also apply to Colors terminal helper
//        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setHighPingMode(TERMINAL_SETTINGS.highPingMode);
//        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setPhoenixClientCompat(TERMINAL_SETTINGS.phoenixClientCompat);
//        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setScale(TERMINAL_SETTINGS.scale);
//        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setTimeoutMs(TERMINAL_SETTINGS.timeoutMs);
//        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setFirstClickBlockMs(TERMINAL_SETTINGS.firstClickMs);
//        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setOffsetX(TERMINAL_SETTINGS.offsetX);
//        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setOffsetY(TERMINAL_SETTINGS.offsetY);
//        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setOverlayColor(new java.awt.Color(
//                TERMINAL_SETTINGS.overlayColor.getRed(),
//                TERMINAL_SETTINGS.overlayColor.getGreen(),
//                TERMINAL_SETTINGS.overlayColor.getBlue(),
//                TERMINAL_SETTINGS.overlayColor.getAlpha()
//        ).getRGB());
//        com.aftertime.ratallofyou.modules.dungeon.terminals.Colors.setBackgroundColor(new java.awt.Color(
//                TERMINAL_SETTINGS.backgroundColor.getRed(),
//                TERMINAL_SETTINGS.backgroundColor.getGreen(),
//                TERMINAL_SETTINGS.backgroundColor.getBlue(),
//                TERMINAL_SETTINGS.backgroundColor.getAlpha()
//        ).getRGB());
//    }
}
