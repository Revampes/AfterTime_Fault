package com.aftertime.ratallofyou.UI.config.ConfigData;


import com.aftertime.ratallofyou.UI.config.ConfigIO;

import java.awt.Color;
import java.lang.reflect.Type;
import java.util.*;

public class AllConfig {
    public static AllConfig INSTANCE = new AllConfig();

    public List<FastHotkeyEntry> FAST_HOTKEY_ENTRIES = new ArrayList<>();

    // Initialize individual config maps first to avoid nulls in ALLCONFIGS
    public final HashMap<String, BaseConfig<?>> COMMAND_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("command_warp", new BaseConfig<>("Warp", "Enable !warp command", false));
        put("command_warp_transfer", new BaseConfig<>("Warp Transfer", "Enable !warptransfer command", false));
        put("command_coords", new BaseConfig<>("Coords", "Enable !coords command", false));
        put("command_all_invite", new BaseConfig<>("All Invite", "Enable !allinvite command", false));
        put("command_boop", new BaseConfig<>("Boop", "Enable !boop command", false));
        put("command_coin_flip", new BaseConfig<>("Coin Flip", "Enable !cf command", false));
        put("command_8ball", new BaseConfig<>("8Ball", "Enable !8ball command", false));
        put("command_dice", new BaseConfig<>("Dice", "Enable !dice command", false));
        put("command_party_transfer", new BaseConfig<>("Party Transfer", "Enable !pt command", false));
        put("command_tps", new BaseConfig<>("TPS", "Enable !tps command", false));
        put("command_downtime", new BaseConfig<>("Downtime", "Enable !dt command", false));
        put("command_queue_instance", new BaseConfig<>("Queue Instance", "Enable dungeon queue commands", false));
        put("command_demote", new BaseConfig<>("Demote", "Enable !demote command", false));
        put("command_promote", new BaseConfig<>("Promote", "Enable !promote command", false));
        put("command_disband", new BaseConfig<>("Disband", "Enable !disband command", false));
        put("command_pt_warp", new BaseConfig<>("pt+warp", "Enable !ptwarp command", false));
    }};

    public final HashMap<String, BaseConfig<?>> NODEBUFF_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("nodebuff_remove_fire_overlay", new BaseConfig<>("Remove Fire Overlay", "Disables the fire overlay effect", false));
        put("nodebuff_ignore_blindness", new BaseConfig<>("Ignore Blindness", "Removes blindness effect", false));
        put("nodebuff_clear_liquid_vision", new BaseConfig<>("Clear Liquid Vision", "Clears water/lava fog", false));
    }};

    public final HashMap<String, BaseConfig<?>> ETHERWARP_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("etherwarp_sync_with_server", new BaseConfig<>("Sync with server", "Sync etherwarp with server position", false));
        put("etherwarp_only_show_when_sneak", new BaseConfig<>("Only show when sneak", "Only show overlay when sneaking", true));
        put("etherwarp_show_fail_location", new BaseConfig<>("Show fail location", "Show where etherwarp would fail", true));
        put("etherwarp_render_method", new BaseConfig<>("Render Method", "Select how to render the overlay", new DataType_DropDown(0,new String[]{"Edges", "Filled", "Both"})));
        put("etherwarp_OverlayColor", new BaseConfig<>("Overlay Color", "Color for Ether Overlay", new Color(0, 255, 0, 200)));
        put("etherwarp_OverlayFailColor", new BaseConfig<>("Fail Overlay Color", "Color for Failed Etherwarp position", new Color(180, 0, 0, 200)));

    }};

    public final HashMap<String, BaseConfig<?>> TERMINAL_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("terminal_high_ping_mode", new BaseConfig<>("Smooth Terminal", "Smooth Terminal GUI Especially for High Ping Users", false));
        put("terminal_scale", new BaseConfig<>("Terminal Scale", "Scale factor for terminal display", 1.0f));
        put("terminal_timeout_ms", new BaseConfig<>("Timeout (ms)", "Timeout in milliseconds for terminal operations", 500));
        put("terminal_first_click_ms", new BaseConfig<>("First Click Delay (ms)", "Delay in milliseconds for first click", 0));
        put("terminal_offset_x", new BaseConfig<>("X Offset", "Horizontal offset for terminal position", 0));
        put("terminal_offset_y", new BaseConfig<>("Y Offset", "Vertical offset for terminal position", 0));
        put("terminal_overlay_color", new BaseConfig<>("Overlay Color", "Color for terminal overlay (RGBA)", new Color(0, 255, 0, 255)));
        put("terminal_background_color", new BaseConfig<>("Background Color", "Background color for terminal (RGBA)", new Color(0, 0, 0, 127)));
        // New: optional rounded corners and high ping queue pacing
        put("terminal_corner_radius_bg", new BaseConfig<>("BG Corner Radius", "Rounded corner radius for terminal background (px)", 1));
        put("terminal_corner_radius_cell", new BaseConfig<>("Cell Corner Radius", "Rounded corner radius for cell highlights (px)", 1));
        put("terminal_high_ping_interval_ms", new BaseConfig<>("High Ping Interval (ms)", "Spacing between queued clicks in Smooth Terminal mode", 120));
        // Per-terminal toggles
        put("terminal_enable_numbers", new BaseConfig<>("Enable Numbers", "Enable Numbers terminal helper GUI", true));
        put("terminal_enable_starts_with", new BaseConfig<>("Enable Starts With", "Enable Starts With terminal helper GUI", true));
        put("terminal_enable_colors", new BaseConfig<>("Enable Colors", "Enable Colors terminal helper GUI", true));
        put("terminal_enable_red_green", new BaseConfig<>("Enable Red Green", "Enable Red Green terminal helper GUI", true));
        put("terminal_enable_rubix", new BaseConfig<>("Enable Rubix", "Enable Rubix terminal helper GUI", true));
        put("terminal_enable_melody", new BaseConfig<>("Enable Melody", "Enable Melody terminal helper GUI", true));
    }};

    public final HashMap<String,BaseConfig<?>> Pos_CONFIGS = new HashMap<String,BaseConfig<?>>()
    {{
        put("bonzo_pos",new BaseConfig<>("Bonzo Mask Display Position","Position Of Bonzo Mask Invincibility Timer Display",new UIPosition(200,200)));
        put("spirit_pos",new BaseConfig<>("Spirit Mask Display Position","Position Of Spirit Mask Invincibility Timer Display",new UIPosition(200,200)));
        put("phoenix_pos",new BaseConfig<>("Phoenix Display Position","Position Of Phoenix Invincibility Timer Display",new UIPosition(200,200)));
        put("proc_pos",new BaseConfig<>("Insta Death Display Position","Position Of Insta Death (Proc) Timer Display",new UIPosition(200,200)));
        put("p3ticktimer_pos",new BaseConfig<>("P3 Tick Timer","P3 Tick Timer",new UIPosition(200,200)));
        put("searchbar_pos", new BaseConfig<>("Search Bar Position", "Position of the inventory search bar", new UIPosition(200, 200)));
        put("searchbar_width", new BaseConfig<>("Search Bar Width", "Width of the inventory search bar", 192));
        put("searchbar_height", new BaseConfig<>("Search Bar Height", "Height of the inventory search bar", 16));
        put("p3ticktimer_scale", new BaseConfig<>("P3 Timer Scale", "Scale factor for P3 Tick Timer", 1.0f));
        put("invincible_scale", new BaseConfig<>("Invincible Timers Scale", "Scale for Bonzo/Spirit/Phoenix/Proc text", 1.0f));
        put("arrowpoison_pos", new BaseConfig<>("Arrow Poison HUD Position", "Position of the Arrow Poison HUD", new UIPosition(200, 200)));
        put("arrowpoison_scale", new BaseConfig<>("Arrow Poison HUD Scale", "Scale of the Arrow Poison HUD", 1.0f));
        put("flareflux_pos", new BaseConfig<>("Flare/Flux HUD Position", "Position of the Flare/Flux timer", new UIPosition(220, 220)));
        put("flareflux_scale", new BaseConfig<>("Flare/Flux HUD Scale", "Scale of the Flare/Flux timer", 1.0f));

    }};

    // New: Kuudra Chest Open Notice sub-settings
    public final HashMap<String, BaseConfig<?>> KUUDRA_CHESTOPEN_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("kuudra_auto_openchest", new BaseConfig<>("Auto Open Chest", "Automatically click Paid Chest (slot 31)", false));
        put("kuudra_auto_requeue", new BaseConfig<>("Auto Requeue at 4", "Auto /instancerequeue when 4 players looted", false));
        put("kuudra_chest_tag", new BaseConfig<>("Chest Tag", "Text inside brackets for chest announcement", "IQ"));
    }};

    // New: Auto Fish sub-settings
    public final HashMap<String, BaseConfig<?>> AUTOFISH_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("autofish_sneak", new BaseConfig<>("Sneak while fishing", "Hold sneak while Auto Fish is enabled", false));
        put("autofish_throw_hook", new BaseConfig<>("Throw if no hook", "Auto-throw rod when no hook is out", true));
        put("autofish_throw_cooldown_s", new BaseConfig<>("Throw cooldown (s)", "Minimum seconds between auto throws", 2));
        put("autofish_rethrow", new BaseConfig<>("Rethrow on timeout", "Reel and re-throw if hook is out too long", true));
        put("autofish_rethrow_cooldown_s", new BaseConfig<>("Rethrow timeout (s)", "Seconds before rethrowing the hook", 25));
        put("autofish_slug_mode", new BaseConfig<>("Slug mode", "Ignore splash sounds; rely on timeout only", false));
        put("autofish_message", new BaseConfig<>("Messages", "Show status messages in chat", false));
        put("autofish_timer", new BaseConfig<>("Show timer", "Show on-screen hook timer", true));
        put("autofish_timer_x", new BaseConfig<>("Timer X", "X position for timer text", 5));
        put("autofish_timer_y", new BaseConfig<>("Timer Y", "Y position for timer text", 5));
        put("autofish_autoshift", new BaseConfig<>("AutoShift", "Periodically tap shift while fishing", false));
        put("autofish_autoshift_interval_s", new BaseConfig<>("AutoShift Interval (s)", "Average seconds between shift taps (±3s random)", 15));
        put("autofish_hotkey", new BaseConfig<>("Toggle Hotkey", "Press a key to bind a toggle for Auto Fish (ESC to clear)", 0));
    }};

    // New: Auto Experiment sub-settings
    public final HashMap<String, BaseConfig<?>> AUTOEXPERIMENT_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("autoexperiment_delay_ms", new BaseConfig<>("Click Delay (ms)", "Delay between automated clicks", 120));
        put("autoexperiment_auto_exit", new BaseConfig<>("Auto Exit (with max serum)", "Close GUI after enough rounds memorized", false));
        put("autoexperiment_debug", new BaseConfig<>("Debug", "Print debug info in chat while solving", false));
    }};

    // New: Player ESP sub-settings
    public final HashMap<String, BaseConfig<?>> PLAYERESP_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("playeresp_mode", new BaseConfig<>("Mode", "ESP render mode", new DataType_DropDown(0, new String[]{"Wireframe", "Filled"})));
        put("playeresp_color", new BaseConfig<>("Color", "ESP color (RGBA)", new Color(0, 255, 255, 200)));
        put("playeresp_only_party", new BaseConfig<>("Only Party Members", "Render ESP only for current party members", false));
    }};

    // New: NameTag sub-settings
    public final HashMap<String, BaseConfig<?>> NAMETAG_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("nametag_scale", new BaseConfig<>("NameTag Scale", "On-screen size scale for player name tags", 0.002f));
        put("nametag_only_party", new BaseConfig<>("Only Party Members", "Render name tag only for current party members", false));
    }};

    public final HashMap<String,BaseConfig<?>> MODULES = new HashMap<String,BaseConfig<?>>()
    {{
        // Kuudra
        put("kuudra_crateaura",new ModuleInfo("Crate Aura", "Suspicious", "Kuudra", false));
        put("kuudra_pearlrefill",new ModuleInfo("Pearl Refill (Use at your own risk!)", "Automatically refill ender pearls", "Kuudra", false));
        put("kuudra_pearlcancel",new ModuleInfo("Pearl Cancel (Use at your own risk!)", "Allow pearl usage when facing floor", "Kuudra", false));
        put("kuudra_cratebeam",new ModuleInfo("Crate Beam", "Draw beams on Kuudra supplies", "Kuudra", false));
        put("kuudra_checknopre", new ModuleInfo("Check No Pre", "Send message if no pre", "Kuudra", false));
        put("kuudra_cratehighlighter",new ModuleInfo("Crate Highlighter", "Box Kuudra crates", "Kuudra", false));
        put("kuudra_pearllineups",new ModuleInfo("Moveable Pearl Lineups", "Show pearl aim spots", "Kuudra", false));
        put("kuudra_fixedpearllineups",new ModuleInfo("Fixed Pos Pearl Lineups", "Show pearl aim spots", "Kuudra", false));
        put("kuudra-freshmessage",new ModuleInfo("Fresh Message", "Sends a message when you get fresh tool", "Kuudra", false));
        put("kuudra_buildpiles",new ModuleInfo("Build Piles", "Show build pile locations", "Kuudra", false));
        put("kuudra_buildbuilders", new ModuleInfo("Builders Count", "Show number of players helping", "Kuudra", false));
        put("kuudra_kuudradirection", new ModuleInfo("Kuudra Directions", "Show directions of kuudra in P5", "Kuudra", false));
        put("kuudra_kuudrahp", new ModuleInfo("Kuudra HP", "Show Kuudra's HP", "Kuudra", false));
        put("kuudra_kuudrahitbox", new ModuleInfo("Kuudra Hitbox", "Show Kuudra's Hitbox", "Kuudra", false));
        put("kuudra_chestopennotice", new ModuleInfo("Chest Open Notice", "Announce/tally chest loots; optional auto-open & requeue", "Kuudra", false));
        put("kuudra_blockuselessperks", new ModuleInfo("Block Useless Perks (not working)", "Hide specified perks in Kuudra Perk Menu", "Kuudra", false));
        put("kuudra_arrowpoison", new ModuleInfo("Arrow Poison Tracker", "HUD showing Twilight/Toxic Arrow Poison and P1 alert", "Kuudra", false));
        put("kuudra_cratepriority", new ModuleInfo("Crate Priority", "Show next action when a crate is missing (No <spot> call)", "Kuudra", false));

        // Dungeons
        put("dungeons_invincibletimer",new ModuleInfo("Invincible Timer", "Show invincibility timers", "Dungeons", false));
        put("dungeons_phase3countdown",new ModuleInfo("Phase 3 Start CountDown", "Timer for phase 3 transitions", "Dungeons", false));
        put("dungeons_phase3ticktimer",new ModuleInfo("Phase 3 Tick Timer", "Track instant damage intervals", "Dungeons", false));
        put("dungeons_sweatmode",new ModuleInfo("Dungeon Sweat Mode", "Recommend only enable it in f7/m7", "Dungeons", false));
        put("dungeons_leapannounce",new ModuleInfo("Leap Announce", "Yes announce", "Dungeons", false));
        put("dungeons_keyhighlighter",new ModuleInfo("Key Highlighter", "Highlights Key", "Dungeons", false));
        put("dungeons_starmobhighlighter",new ModuleInfo("Star Mob Highlighter", "Highlights starred mobs and Shadow Assassins", "Dungeons", false));
        put("dungeons_secretclicks",new ModuleInfo("Show Secret Clicks", "Highlights when you click on secrets", "Dungeons", false));
        put("dungeons_terminals",new ModuleInfo("Dungeon Terminals", "Custom GUI and solver for terminals", "Dungeons", false));
        put("dungeons_watcherclear", new ModuleInfo("Watcher Clear", "Delay then countdown after Watcher opens blood", "Dungeons", false));
        put("dungeons_customleapmenu", new ModuleInfo("Custom Leap Menu (not working)", "Replace Spirit Leap GUI with a faster teammate list", "Dungeons", false));
        put("dungeons_healerwishalert", new ModuleInfo("Healer Wish Alert", "Alert when needed to wish", "Dungeons", false));
        put("dungeons_findcorrectlivid", new ModuleInfo("Find Correct Livid (not working)", "Highlights the correct Livid in Floor 5 boss fight", "Dungeons", false));

        // SkyBlock
        put("skyblock_partycommands",new ModuleInfo("Party Commands", "Only work in party chat", "SkyBlock", false));
        put("skyblock_waypointgrab", new ModuleInfo("Waypoint", "Render beacon beam for waypoints", "SkyBlock", false));
        put("skyblock_autosprint",new ModuleInfo("Toggle Sprint", "Automatically sprint when moving", "SkyBlock", false));
        put("skyblock_fasthotkey",new ModuleInfo("Fast Hotkey", "Fast hotkey switching", "SkyBlock", false));
        put("skyblock_hotbarswap", new ModuleInfo("Hotbar Swap", "Swap to saved hotbar via keybind or message", "SkyBlock", false));
        put("skyblock_searchbar", new ModuleInfo("Inventory Search Bar", "Search and highlight items in open containers", "SkyBlock", false));
        put("skyblock_flareflux", new ModuleInfo("Flare/Flux Timer", "Detect nearby Flux or Flare and show a timer/label", "SkyBlock", false));
        put("skyblock_storageoverview", new ModuleInfo("Storage Overview", "Left-panel overlay showing Ender Chests/Backpacks contents", "SkyBlock", false));
        put("skyblock_autoexperiment", new ModuleInfo("Auto Experiment", "Chronomatron/Ultrasequencer helper (Use at your own risk)", "SkyBlock", false));
        put("skyblock_superpairs", new ModuleInfo("Super Pairs", "Highlight matching pairs in the Superpairs experiment", "SkyBlock", false));
        //Fishing
        put("fishing_autofish", new ModuleInfo("Auto Fish", "Automatically fish: reel on splash and re-throw", "Fishing", false));

        // Render
        put("render_fullbright",new ModuleInfo("FullBright", "SHINE!", "Render", false));
        put("render_nodebuff",new ModuleInfo("No Debuff", "Removes negative effects", "Render", false));
        put("render_etherwarpoverlay",new ModuleInfo("Etherwarp Overlay", "Shows where you'll teleport with etherwarp", "Render", false));
        put("render_playeresp", new ModuleInfo("Player ESP", "Highlight other players with boxes/glow", "Render", false));
        put("render_nametag", new ModuleInfo("NameTag", "Render name tags for players (filters NPCs)", "Render", false));
        put("render_customcape", new ModuleInfo("Custom Cape", "Load custom cape from capes/ directory (PNG/GIF)", "Render", false));
        put("render_darkmode", new ModuleInfo("DarkMode", "idk", "Render", false));

        //Performance
        put("performance_hideuselessmsg",new ModuleInfo("Hide Useless Message", "Hide Message Yes!", "Performance", false));
        put("performance_hidelightning", new ModuleInfo("Hide Lightning", "Hide lightning bolt renders", "Performance", false));

        // GUI
        put("gui_moveguiposition",new ModuleInfo("Move GUI Position", "Enable dragging of UI elements", "GUI", false));
    }};

    // New: Fast Hotkey appearance/config options
    public final HashMap<String, BaseConfig<?>> FASTHOTKEY_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("fhk_inner_radius", new BaseConfig<>("Inner Radius", "Inner cancel circle radius (px)", 40));
        put("fhk_outer_radius", new BaseConfig<>("Outer Radius", "Outer ring radius (px)", 150));
        put("fhk_inner_near_color", new BaseConfig<>("Inner Near Color", "Color nearest to cursor on inner ring (RGBA)", new Color(255, 255, 255, 255)));
        put("fhk_inner_far_color", new BaseConfig<>("Inner Far Color", "Color farthest from cursor on inner ring (RGBA)", new Color(0, 0, 0, 255)));
        put("fhk_outer_near_color", new BaseConfig<>("Outer Near Color", "Color nearest to cursor on outer ring (RGBA)", new Color(255, 255, 255, 255)));
        put("fhk_outer_far_color", new BaseConfig<>("Outer Far Color", "Color farthest from cursor on outer ring (RGBA)", new Color(0, 0, 0, 255)));
        put("fhk_outline_prox_range", new BaseConfig<>("Outline Proximity Range", "How far from ring counts as close (px)", 120));
        put("fhk_bg_near_color", new BaseConfig<>("BG Near Color", "Background color near cursor (RGBA, alpha drives intensity)", new Color(96, 96, 96, 128)));
        put("fhk_bg_far_color", new BaseConfig<>("BG Far Color", "Background color far from cursor on outer ring (RGBA, light grey)", new Color(160, 160, 160, 128)));
        put("fhk_bg_influence_radius", new BaseConfig<>("BG Influence Radius", "Distance where background hover fades out (px)", 60));
        put("fhk_bg_max_extend", new BaseConfig<>("BG Max Extend", "Max outward extension of background near cursor (px)", 14.0f));
        put("fhk_show_arrow", new BaseConfig<>("Show Arrow", "Show direction arrow near inner ring", true));
    }};

    // New: Hotbar Swap settings
    public final HashMap<String, BaseConfig<?>> HOTBARSWAP_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("hotbarswap_enable_chat_triggers", new BaseConfig<>("Enable Chat Triggers", "Trigger presets from exact chat messages", true));
        put("hotbarswap_enable_keybinds", new BaseConfig<>("Enable Keybinds", "Trigger presets from keybinds", true));
        put("hotbarswap_block_ticks", new BaseConfig<>("Block Movement Ticks", "Ticks to suppress movement after a swap", 10));
    }};

    // New: Storage Overview settings
    public final HashMap<String, BaseConfig<?>> STORAGEOVERVIEW_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("storageoverview_show_in_inventory", new BaseConfig<>("Show In Inventory", "Show overlay when player inventory is open", true));
    }};

    public final HashMap<String, BaseConfig<?>> DARKMODE_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("darkmode_getopacity", new BaseConfig<>("Adjust opacity", "Adjust the opacity of dark mode from 0-255", 128));
    }};

    public final HashMap<String, BaseConfig<?>> CUSTOMCAPE_CONFIGS = new HashMap<String, BaseConfig<?>>()
    {{
        put("customcape_reloadcape", new BaseConfig<>("Reload Cape", "Reload Cape", false));
    }};

    // New: Fast Hotkey presets model and active pointer
    public List<FastHotkeyPreset> FHK_PRESETS = new ArrayList<>();
    public int FHK_ACTIVE_PRESET = 0;
    // After individual maps are ready, build the index map
    public final HashMap<Integer,HashMap<String,BaseConfig<?>>> ALLCONFIGS = new HashMap<Integer,HashMap<String,BaseConfig<?>>> () {{
        put(0, COMMAND_CONFIGS);
        put(1, MODULES);
        put(2, NODEBUFF_CONFIGS);
        put(3, ETHERWARP_CONFIGS);
        put(4, TERMINAL_CONFIGS);
        put(5, Pos_CONFIGS);
        put(6, FASTHOTKEY_CONFIGS);
        put(7, KUUDRA_CHESTOPEN_CONFIGS);
        put(8, HOTBARSWAP_CONFIGS);
        put(9, STORAGEOVERVIEW_CONFIGS);
        put(10, AUTOFISH_CONFIGS);
        put(11, AUTOEXPERIMENT_CONFIGS);
        put(12, PLAYERESP_CONFIGS);
        put(13, NAMETAG_CONFIGS);
        put(14, CUSTOMCAPE_CONFIGS);
        put(15, DARKMODE_CONFIGS);
    }};

    public final List<String> Categories = new ArrayList<String>()
    {{
        add("Kuudra");
        add("Dungeons");
        add("SkyBlock");
        add("Render");
        add("Performance");
        add("Fishing");
        add("GUI");
    }};

    public void LoadFromProperty(Properties properties,Properties fhk_properties)
    {
        // Load each known config key from properties using composite key "index,RealKey"
        for (Map.Entry<Integer, HashMap<String, BaseConfig<?>>> groupEntry : ALLCONFIGS.entrySet()) {
            int index = groupEntry.getKey();
            for (Map.Entry<String, BaseConfig<?>> entry : groupEntry.getValue().entrySet()) {
                String realKey = entry.getKey();
                String compositeKey = index + "," + realKey;
                BaseConfig<?> cfg = entry.getValue();
                Type type = cfg.type;
                Object value = ConfigIO.INSTANCE.GetConfig(compositeKey, type);
                if (value != null) {
                    // unchecked on purpose but safe by construction of type
                    @SuppressWarnings("unchecked")
                    BaseConfig<Object> cfgObj = (BaseConfig<Object>) cfg;
                    cfgObj.Data = value;
                }
            }
        }
        // Load FastHotkey Entries (backward compatible presets)
        // Prefer new presets storage; fallback to legacy single list
        List<FastHotkeyPreset> loaded = com.aftertime.ratallofyou.UI.config.ConfigIO.INSTANCE.LoadFastHotKeyPresets();
        if (loaded != null && !loaded.isEmpty()) {
            FHK_PRESETS = loaded;
            FHK_ACTIVE_PRESET = Math.max(0, Math.min(com.aftertime.ratallofyou.UI.config.ConfigIO.INSTANCE.GetActiveFhkPresetIndex(loaded.size()), loaded.size() - 1));
            FAST_HOTKEY_ENTRIES = FHK_PRESETS.get(FHK_ACTIVE_PRESET).entries;
        } else {
            FAST_HOTKEY_ENTRIES = com.aftertime.ratallofyou.UI.config.ConfigIO.INSTANCE.LoadFastHotKeyEntries();
            FHK_PRESETS.clear();
            FastHotkeyPreset def = new FastHotkeyPreset("Default");
            def.entries.addAll(FAST_HOTKEY_ENTRIES);
            FHK_PRESETS.add(def);
            FHK_ACTIVE_PRESET = 0;
        }

    }
    public void SaveToProperty()
    {
        for(Map.Entry<Integer,HashMap<String,BaseConfig<?>>> bc : ALLCONFIGS.entrySet()) {
            int index = bc.getKey();
            for (Map.Entry<String, BaseConfig<?>> entry : bc.getValue().entrySet()) {
                BaseConfig<?> config = entry.getValue();
                String key = index + "," + entry.getKey();
                com.aftertime.ratallofyou.UI.config.ConfigIO.INSTANCE.SetConfig(key, config.Data);
            }
        }
        // Persist Fast Hotkey presets and active pointer
        com.aftertime.ratallofyou.UI.config.ConfigIO.INSTANCE.SaveFastHotKeyPresets(FHK_PRESETS, FHK_ACTIVE_PRESET);

    }

    // Helper to switch active preset safely and keep alias list in sync
    public void setActiveFhkPreset(int idx) {
        if (FHK_PRESETS == null || FHK_PRESETS.isEmpty()) return;
        int clamped = Math.max(0, Math.min(idx, FHK_PRESETS.size() - 1));
        FHK_ACTIVE_PRESET = clamped;
        FAST_HOTKEY_ENTRIES = FHK_PRESETS.get(FHK_ACTIVE_PRESET).entries;
        // save immediately to keep runtime in sync
        com.aftertime.ratallofyou.UI.config.ConfigIO.INSTANCE.SaveFastHotKeyPresets(FHK_PRESETS, FHK_ACTIVE_PRESET);
    }
}
