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
        put("terminal_phoenix_client_compat", new BaseConfig<>("Phoenix Client Compatibility", "Enable compatibility with Phoenix Client", false));
        put("terminal_scale", new BaseConfig<>("Terminal Scale", "Scale factor for terminal display", 1.0f));
        put("terminal_timeout_ms", new BaseConfig<>("Timeout (ms)", "Timeout in milliseconds for terminal operations", 500));
        put("terminal_first_click_ms", new BaseConfig<>("First Click Delay (ms)", "Delay in milliseconds for first click", 0));
        put("terminal_offset_x", new BaseConfig<>("X Offset", "Horizontal offset for terminal position", 0));
        put("terminal_offset_y", new BaseConfig<>("Y Offset", "Vertical offset for terminal position", 0));
        put("terminal_overlay_color", new BaseConfig<>("Overlay Color", "Color for terminal overlay (RGBA)", new Color(0, 255, 0, 255)));
        put("terminal_background_color", new BaseConfig<>("Background Color", "Background color for terminal (RGBA)", new Color(0, 0, 0, 127)));
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

    }};

    public final HashMap<String,BaseConfig<?>> MODULES = new HashMap<String,BaseConfig<?>>()
    {{
            // Kuudra
            put("kuudra_pearlrefill",new ModuleInfo("Pearl Refill (Use at your own risk!)", "Automatically refill ender pearls", "Kuudra", false));
            put("kuudra_pearlcancel",new ModuleInfo("Pearl Cancel (Use at your own risk!)", "Allow pearl usage when facing floor", "Kuudra", false));
            put("kuudra_cratebeam",new ModuleInfo("Crate Beam", "Draw beams on Kuudra supplies", "Kuudra", false));
            put("kuudra_cratehighlighter",new ModuleInfo("Crate Highlighter", "Box Kuudra crates", "Kuudra", false));
            put("kuudra_pearllineups",new ModuleInfo("Pearl Lineups", "Show pearl aim spots", "Kuudra", false));

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

            // SkyBlock
            put("skyblock_partycommands",new ModuleInfo("Party Commands", "Only work in party chat", "SkyBlock", false));
            put("skyblock_autosprint",new ModuleInfo("Toggle Sprint", "Automatically sprint when moving", "SkyBlock", false));
            put("skyblock_fasthotkey",new ModuleInfo("Fast Hotkey", "Fast hotkey switching", "SkyBlock", false));

            // Render
            put("render_fullbright",new ModuleInfo("FullBright", "SHINE!", "Render", false));
            put("render_nodebuff",new ModuleInfo("No Debuff", "Removes negative effects", "Render", false));
            put("render_etherwarpoverlay",new ModuleInfo("Etherwarp Overlay", "Shows where you'll teleport with etherwarp", "Render", false));

            // GUI
            put("gui_moveguiposition",new ModuleInfo("Move GUI Position", "Enable dragging of UI elements", "GUI", false));
    }};

    // After individual maps are ready, build the index map
    public final HashMap<Integer,HashMap<String,BaseConfig<?>>> ALLCONFIGS = new HashMap<Integer,HashMap<String,BaseConfig<?>>>()
    {{
        put(0, COMMAND_CONFIGS);
        put(1, MODULES);
        put(2, NODEBUFF_CONFIGS);
        put(3, ETHERWARP_CONFIGS);
        put(4, TERMINAL_CONFIGS);
        put(5, Pos_CONFIGS);
    }};

    public final List<String> Categories = new ArrayList<String>()
    {{
        add("Kuudra");
        add("Dungeons");
        add("SkyBlock");
        add("Render");
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
        // Load FastHotkey Entries
        FAST_HOTKEY_ENTRIES = ConfigIO.INSTANCE.LoadFastHotKeyEntries();

    }
    public void SaveToProperty()
    {
        for(Map.Entry<Integer,HashMap<String,BaseConfig<?>>> bc : ALLCONFIGS.entrySet()) {
            int index = bc.getKey();
            for (Map.Entry<String, BaseConfig<?>> entry : bc.getValue().entrySet()) {
                BaseConfig<?> config = entry.getValue();
                String key = index + "," + entry.getKey();
                ConfigIO.INSTANCE.SetConfig(key, config.Data);
            }
        }
            ConfigIO.INSTANCE.SaveFastHotKeyEntries(FAST_HOTKEY_ENTRIES);

    }

}
