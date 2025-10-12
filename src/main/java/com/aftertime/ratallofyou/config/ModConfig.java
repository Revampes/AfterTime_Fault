package com.aftertime.ratallofyou.config;

import com.aftertime.ratallofyou.UI.newui.annotations.*;

public class ModConfig {

    @ToggleButton(
            key = "render-dark-mode",
            name = "Dark Mode",
            description = "Enable dark mode overlay",
            category = "Render"
    )
    public static boolean enableDarkMode = false;

    @Slider(
            key = "render-dark-mode",
            min = 0,
            max = 255
    )
    public static int darkModeOpacity = 128;

    @ToggleButton(
            key = "render-custom-cape",
            name = "Custom Cape",
            description = "Customizable optifine cape",
            category = "Render"
    )
    public static boolean enableCustomCape = false;

    // Subsettings for Custom Cape
    @Slider(
            key = "render-custom-cape",
            min = 1,
            max = 20
    )
    public static int customCapeFrameDelay = 5; // ticks between frames for animated GIF

    // Etherwarp Overlay
    @ToggleButton(
            key = "render-etherwarpoverlay",
            name = "Etherwarp Overlay",
            description = "Highlights etherwarp destination",
            category = "Render"
    )
    public static boolean enableEtherwarpOverlay = false;

    @CheckBox(
            key = "render-etherwarpoverlay",
            title = "Only show when sneaking"
    )
    public static boolean etherwarpOnlySneak = true;

    @CheckBox(
            key = "render-etherwarpoverlay",
            title = "Sync with server position"
    )
    public static boolean etherwarpSyncWithServer = false;

    @CheckBox(
            key = "render-etherwarpoverlay",
            title = "Show fail location"
    )
    public static boolean etherwarpShowFailLocation = true;

    @DropdownBox(
            key = "render-etherwarpoverlay",
            title = "Highlight Type",
            options = {"Edges", "Filled", "Both"}
    )
    public static int etherwarpHighlightType = 0;

    @ColorPicker(
            key = "render-etherwarpoverlay",
            title = "Overlay Color"
    )
    public static int etherwarpOverlayColor = new java.awt.Color(0, 255, 0, 200).getRGB();

    @ColorPicker(
            key = "render-etherwarpoverlay",
            title = "Fail Color"
    )
    public static int etherwarpOverlayFailColor = new java.awt.Color(180, 0, 0, 200).getRGB();

    // NameTag
    @ToggleButton(
            key = "render-nametag",
            name = "NameTag",
            description = "Render floating nametags for players",
            category = "Render"
    )
    public static boolean enableNameTag = false;

    // Represent as percentage (1..100), actual scale = value / 1000f
    @Slider(
            key = "render-nametag",
            min = 1,
            max = 100
    )
    public static int nametagScalePercent = 20; // 20 -> 0.020, and we further reduce in renderer

    @CheckBox(
            key = "render-nametag",
            title = "Only show for party members"
    )
    public static boolean nametagOnlyParty = false;

    // NoDebuff
    @ToggleButton(
            key = "render-nodebuff",
            name = "No Debuff",
            description = "Remove blindness, fire overlay, and liquid fog",
            category = "Render"
    )
    public static boolean enableNoDebuff = false;

    @CheckBox(
            key = "render-nodebuff",
            title = "Ignore blindness overlay"
    )
    public static boolean nodebuffIgnoreBlindness = true;

    @CheckBox(
            key = "render-nodebuff",
            title = "Remove fire overlay"
    )
    public static boolean nodebuffRemoveFireOverlay = true;

    @CheckBox(
            key = "render-nodebuff",
            title = "Clear water/lava vision"
    )
    public static boolean nodebuffClearLiquidVision = true;

    // SkyBlock Mark Location
    @ToggleButton(
            key = "skyblock-mark-location",
            name = "Mark Location",
            description = "Generate coordinates on where you looking at",
            category = "SkyBlock"
    )
    public static boolean enabledMarkLocation = false;

    @KeyBindInput(
            key = "skyblock-mark-location",
            title = "KeyBind"
    )
    public static String markLocationKeyBind = "U";

    // Fishing Auto Fish
    @ToggleButton(
            key = "fishing-auto-fish",
            name = "Auto Fish",
            description = "Automatically reel and cast your rod",
            category = "Fishing"
    )
    public static boolean enabledAutoFish = false;

    @CheckBox(
            key = "fishing-auto-fish",
            title = "Enable auto shift"
    )
    public static boolean enabledAutoShift = false;

    // === Fullbright ===
    @ToggleButton(
            key = "render-fullbright",
            name = "Fullbright",
            description = "Set gamma to max for full brightness",
            category = "Render"
    )
    public static boolean enableFullbright = false;

    // === NoHurtCam ===
    @ToggleButton(
            key = "render-nohurtcam",
            name = "No Hurt Cam",
            description = "Disable hurt camera shake",
            category = "Render"
    )
    public static boolean enableNoHurtCam = false;

    // === PlayerESP ===
    @ToggleButton(
            key = "render-playeresp",
            name = "Player ESP",
            description = "Highlight other players with a box",
            category = "Render"
    )
    public static boolean enablePlayerESP = false;

    @DropdownBox(
            key = "render-playeresp",
            title = "ESP Mode",
            options = {"Wireframe", "Filled"}
    )
    public static int playerESPMode = 0;

    @ColorPicker(
            key = "render-playeresp",
            title = "ESP Color"
    )
    public static int playerESPColor = new java.awt.Color(0, 255, 255, 200).getRGB();

    @CheckBox(
            key = "render-playeresp",
            title = "Only show for party members"
    )
    public static boolean playerESPOnlyParty = false;

    // === Dungeon Modules ===

    @ToggleButton(
        key = "dungeon-autosell",
        name = "Auto Sell",
        description = "Automatically sell specified items in dungeons",
        category = "Dungeon"
    )
    public static boolean enableAutoSell = false;

    @Slider(
        key = "dungeon-autosell",
        min = 300,
        max = 5000
    )
    public static int autoSellDelayMs = 1000; // Delay in ms between sells

    @DropdownBox(
        key = "dungeon-autosell",
        title = "Sell Click Type",
        options = {"Shift Click", "Middle Click", "Left Click"}
    )
    public static int autoSellClickType = 0;

    @CheckBox(
        key = "dungeon-autosell",
        title = "Use Default Item List"
    )
    public static boolean autoSellUseDefaultItems = true;

    @TextInputField(
        key = "dungeon-autosell",
        title = "Custom Items (comma separated)"
    )
    public static String autoSellCustomItems = "";

    @ToggleButton(
        key = "dungeon-f7ghostblocks",
        name = "F7 Ghost Blocks",
        description = "Enable F7 ghost block placements",
        category = "Dungeon"
    )
    public static boolean enableF7GhostBlocks = false;

    @ToggleButton(
        key = "dungeon-findcorrectlivid",
        name = "Find Correct Livid",
        description = "Highlight the correct Livid in F5/M5",
        category = "Dungeon"
    )
    public static boolean enableFindCorrectLivid = false;

    @ToggleButton(
        key = "dungeon-goldorstarttimer",
        name = "Goldor Start Timer",
        description = "Show timer for Goldor phase start",
        category = "Dungeon"
    )
    public static boolean enableGoldorStartTimer = false;

    @ToggleButton(
        key = "dungeon-healerwishalert",
        name = "Healer Wish Alert",
        description = "Alert for healer wish in F7",
        category = "Dungeon"
    )
    public static boolean enableHealerWishAlert = false;

    @ToggleButton(
        key = "dungeon-invincibletimer",
        name = "Invincible Timer",
        description = "Show invincibility proc timers",
        category = "Dungeon"
    )
    public static boolean enableInvincibleTimer = false;

    @ToggleButton(
        key = "dungeon-keyhighlighter",
        name = "Key Highlighter",
        description = "Highlight dungeon keys (Wither/Blood)",
        category = "Dungeon"
    )
    public static boolean enableKeyHighlighter = false;

    @ToggleButton(
        key = "dungeon-leapannounce",
        name = "Leap Announce",
        description = "Announce leaps in party chat",
        category = "Dungeon"
    )
    public static boolean enableLeapAnnounce = false;

    @ToggleButton(
        key = "dungeon-p3ticktimer",
        name = "P3 Tick Timer",
        description = "Show timer for P3 phase in dungeons",
        category = "Dungeon"
    )
    public static boolean enableP3TickTimer = false;

    @ToggleButton(
        key = "dungeon-salvageitem",
        name = "Salvage Item Highlighter",
        description = "Highlight salvageable items in dungeons",
        category = "Dungeon"
    )
    public static boolean enableSalvageItem = false;

    @ToggleButton(
        key = "dungeon-secretclicks",
        name = "Secret Clicks Highlighter",
        description = "Highlight secret clickable blocks in dungeons",
        category = "Dungeon"
    )
    public static boolean enableSecretClicks = false;

    @ColorPicker(
        key = "dungeon-secretclicks-color",
        title = "Secret Highlight Color"
    )
    public static int secretClicksHighlightColor = new java.awt.Color(0, 255, 0, 128).getRGB();

    @ToggleButton(
        key = "dungeon-starmobhighlighter",
        name = "Star Mob Highlighter",
        description = "Highlight starred mobs in dungeons",
        category = "Dungeon"
    )
    public static boolean enableStarMobHighlighter = false;

    @ToggleButton(
        key = "dungeon-watcherclear",
        name = "Watcher Clear Alert",
        description = "Alert for Watcher clear in dungeons",
        category = "Dungeon"
    )
    public static boolean enableWatcherClear = false;

    @ToggleButton(
        key = "dungeon-customleapmenu",
        name = "Custom Leap Menu",
        description = "Replace spirit leap GUI with a custom menu",
        category = "Dungeon"
    )
    public static boolean enableCustomLeapMenu = false;

    // === Performance Modules ===
    @ToggleButton(
        key = "performance-hidelightning",
        name = "Hide Lightning",
        description = "Hide lightning bolt entities to reduce visual noise",
        category = "Performance"
    )
    public static boolean enableHideLightning = false;

    @ToggleButton(
        key = "performance-hideuselessmsg",
        name = "Hide Useless Messages",
        description = "Filter out common spammy chat messages",
        category = "Performance"
    )
    public static boolean enableHideUselessMessages = false;

    // === Kuudra Modules ===
    @ToggleButton(
        key = "kuudra-refillpearls",
        name = "Refill Pearls",
        description = "Auto refill ender pearls from sacks during Kuudra",
        category = "Kuudra"
    )
    public static boolean enableKuudraPearlRefill = false;

    @ToggleButton(
        key = "kuudra-pearlcancel",
        name = "Pearl Cancel",
        description = "Prevent misclicking pearls on blocks in Kuudra",
        category = "Kuudra"
    )
    public static boolean enableKuudraPearlCancel = false;

    @ToggleButton(
        key = "kuudra-kuudrahp",
        name = "Kuudra HP HUD",
        description = "Show Kuudra HP percent on HUD and floating text",
        category = "Kuudra"
    )
    public static boolean enableKuudraHP = false;

    @ToggleButton(
        key = "kuudra-kuudrahitbox",
        name = "Kuudra Hitbox ESP",
        description = "Draw an outline box around Kuudra",
        category = "Kuudra"
    )
    public static boolean enableKuudraHitbox = false;

    @ToggleButton(
        key = "kuudra-kuudradirection",
        name = "Kuudra Direction Title",
        description = "Show LEFT/RIGHT/FRONT/BACK title near 25k HP",
        category = "Kuudra"
    )
    public static boolean enableKuudraDirection = false;

    @ToggleButton(
        key = "kuudra-freshmessage",
        name = "Fresh Message Announce",
        description = "Announce Fresh Tools boost in Phase 2",
        category = "Kuudra"
    )
    public static boolean enableKuudraFreshMessage = false;

    @ToggleButton(
        key = "kuudra-cratepriority",
        name = "Crate Priority Titles",
        description = "Show priority instructions for missing supplies in Phase 1",
        category = "Kuudra"
    )
    public static boolean enableKuudraCratePriority = false;

    @ToggleButton(
        key = "kuudra-arrowpoison",
        name = "Arrow Poison HUD",
        description = "Show Twilight/Toxic Arrow Poison counts and alert",
        category = "Kuudra"
    )
    public static boolean enableKuudraArrowPoison = false;

    // New: Kuudra Chest Open Notice + options
    @ToggleButton(
        key = "kuudra-chestopennotice",
        name = "Chest Open Notice",
        description = "Auto-open Paid Chest, announce loot, and optionally requeue",
        category = "Kuudra"
    )
    public static boolean enableKuudraChestOpenNotice = false;

    @CheckBox(
        key = "kuudra-chestopennotice",
        title = "Auto open Paid Chest"
    )
    public static boolean kuudraAutoOpenChest = false;

    @CheckBox(
        key = "kuudra-chestopennotice",
        title = "Auto /instancerequeue at 4/4"
    )
    public static boolean kuudraAutoRequeue = false;

    @TextInputField(
        key = "kuudra-chestopennotice",
        title = "Chest Tag (party announce)"
    )
    public static String kuudraChestTag = "IQ";

    // New: Kuudra Block Useless Perks
    @ToggleButton(
        key = "kuudra-blockuselessperks",
        name = "Block Useless Perks",
        description = "Hide commonly useless perks in Kuudra Perk Menu",
        category = "Kuudra"
    )
    public static boolean enableKuudraBlockUselessPerks = false;

    // === Layout ===
    @ToggleButton(
        key = "layout-hud",
        name = "HUD Layout",
        description = "Open the HUD layout editor to move on-screen modules",
        category = "Layout"
    )
    public static boolean enableHudLayoutModule = false;

    @NormalButton(
        key = "layout-hud",
        title = "Open HUD Layout",
        action = "com.aftertime.ratallofyou.UI.newui.layout.NewUILayout#open"
    )
    public static boolean openHudLayoutButton = false;

    // ===================== SkyBlock Modules (new) =====================

    // Auto Sprint
    @ToggleButton(
        key = "skyblock-autosprint",
        name = "Auto Sprint",
        description = "Hold sprint automatically; press sprint key to toggle temp disable",
        category = "SkyBlock"
    )
    public static boolean enableAutoSprint = false;

    // Auto Experiment
    @ToggleButton(
        key = "skyblock-autoexperiment",
        name = "Auto Experiment",
        description = "Assist Chronomatron/Ultrasequencer clicks",
        category = "SkyBlock"
    )
    public static boolean enableAutoExperiment = false;

    @Slider(
        key = "skyblock-autoexperiment",
        min = 60,
        max = 1000
    )
    public static int autoExperimentDelayMs = 120;

    @CheckBox(
        key = "skyblock-autoexperiment",
        title = "Auto exit after long sequences"
    )
    public static boolean autoExperimentAutoExit = false;

    @CheckBox(
        key = "skyblock-autoexperiment",
        title = "Debug chat"
    )
    public static boolean autoExperimentDebug = false;

    // Chat Commands
    @ToggleButton(
        key = "skyblock-chatcommands",
        name = "Chat Commands",
        description = "Enable party chat command helpers (!warp, !pt, etc.)",
        category = "SkyBlock"
    )
    public static boolean enableChatCommands = false;

    @CheckBox(key = "skyblock-chatcommands", title = "Coords (!coords)")
    public static boolean chatCmdCoords = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Boop (!boop)")
    public static boolean chatCmdBoop = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Coin Flip (!cf)")
    public static boolean chatCmdCoinFlip = true;
    @CheckBox(key = "skyblock-chatcommands", title = "8 Ball (!8ball)")
    public static boolean chatCmd8Ball = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Dice (!dice)")
    public static boolean chatCmdDice = true;
    @CheckBox(key = "skyblock-chatcommands", title = "TPS (!tps)")
    public static boolean chatCmdTps = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Warp (leader) (!warp)")
    public static boolean chatCmdWarp = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Warp+Transfer (leader) (!wt)")
    public static boolean chatCmdWarpTransfer = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Allinvite toggle (leader)")
    public static boolean chatCmdAllInvite = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Party Transfer (leader) (!pt)")
    public static boolean chatCmdPartyTransfer = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Downtime tracker (!dt)")
    public static boolean chatCmdDowntime = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Queue instance (leader)")
    public static boolean chatCmdQueueInstance = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Demote (leader)")
    public static boolean chatCmdDemote = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Promote (leader)")
    public static boolean chatCmdPromote = true;
    @CheckBox(key = "skyblock-chatcommands", title = "Disband (leader)")
    public static boolean chatCmdDisband = true;
    @CheckBox(key = "skyblock-chatcommands", title = "PT and Warp (!ptw)")
    public static boolean chatCmdPtAndWarp = true;

    // Flux Flare Timer HUD
    @ToggleButton(
        key = "skyblock-fluxflare",
        name = "Flux/Flare HUD",
        description = "Show nearby Flux or Flare time/label",
        category = "SkyBlock"
    )
    public static boolean enableFluxFlareTimer = false;

    // Search Bar in inventory chests
    @ToggleButton(
        key = "skyblock-searchbar",
        name = "Chest Search Bar",
        description = "Filter chest inventory by name/lore; type math to calc",
        category = "SkyBlock"
    )
    public static boolean enableSearchBar = false;

    @Slider(key = "skyblock-searchbar", min = 120, max = 320)
    public static int searchbarWidth = 192;
    @Slider(key = "skyblock-searchbar", min = 12, max = 20)
    public static int searchbarHeight = 16;

    // SuperPairs helper overlay
    @ToggleButton(
        key = "skyblock-superpairs",
        name = "Superpairs Helper",
        description = "Track reveals and highlight pairs",
        category = "SkyBlock"
    )
    public static boolean enableSuperPairs = false;

    // Waypoint Grab from chat coords
    @ToggleButton(
        key = "skyblock-waypointgrab",
        name = "Waypoint Grab",
        description = "Render beam and box at party coords",
        category = "SkyBlock"
    )
    public static boolean enableWaypointGrab = false;

    // Storage Overview
    @ToggleButton(
        key = "skyblock-storageoverview",
        name = "Storage Overview",
        description = "Show Ender Chests and Backpacks overlay",
        category = "SkyBlock"
    )
    public static boolean enableStorageOverview = false;

    @CheckBox(
        key = "skyblock-storageoverview",
        title = "Show in inventory"
    )
    public static boolean storageOverviewShowInInventory = true;

    // HotbarSwap (advanced)
    @ToggleButton(
        key = "skyblock-hotbarswap",
        name = "Hotbar Swap",
        description = "Swap items to preset hotbar layouts",
        category = "SkyBlock"
    )
    public static boolean enableHotbarSwap = false;

    @CheckBox(key = "skyblock-hotbarswap", title = "Enable Keybind Triggers")
    public static boolean hotbarswapEnableKeybinds = true;
    @CheckBox(key = "skyblock-hotbarswap", title = "Enable Chat Triggers")
    public static boolean hotbarswapEnableChatTriggers = true;
    @Slider(key = "skyblock-hotbarswap", min = 0, max = 20)
    public static int hotbarswapBlockTicks = 10;

    // Fast HotKey (radial menu)
    @ToggleButton(
        key = "skyblock-fasthotkey",
        name = "Fast Hotkey",
        description = "Radial quick-commands with hold-and-release",
        category = "SkyBlock"
    )
    public static boolean enableFastHotKey = false;

    @NormalButton(
        key = "skyblock-fasthotkey",
        title = "Open Fast Hotkey Editor",
        action = "com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey.FastHotKeyGui#open"
    )
    public static boolean openFastHotKeyEditor = false;

    @Slider(key = "skyblock-fasthotkey", min = 10, max = 400)
    public static int fhkInnerRadius = 40;
    @Slider(key = "skyblock-fasthotkey", min = 60, max = 600)
    public static int fhkOuterRadius = 150;
    @Slider(key = "skyblock-fasthotkey", min = 10, max = 2000)
    public static int fhkOutlineProxRange = 120;

    @ColorPicker(key = "skyblock-fasthotkey", title = "Inner Near Color")
    public static int fhkInnerNearColor = new java.awt.Color(255,255,255,255).getRGB();
    @ColorPicker(key = "skyblock-fasthotkey", title = "Inner Far Color")
    public static int fhkInnerFarColor = new java.awt.Color(0,0,0,255).getRGB();
    @ColorPicker(key = "skyblock-fasthotkey", title = "Outer Near Color")
    public static int fhkOuterNearColor = new java.awt.Color(255,255,255,255).getRGB();
    @ColorPicker(key = "skyblock-fasthotkey", title = "Outer Far Color")
    public static int fhkOuterFarColor = new java.awt.Color(0,0,0,255).getRGB();

    @CheckBox(key = "skyblock-fasthotkey", title = "Show Direction Arrow")
    public static boolean fhkShowArrow = true;

    @Slider(key = "skyblock-fasthotkey", min = 20, max = 1000)
    public static int fhkBgInfluenceRadius = 30; // px
    @Slider(key = "skyblock-fasthotkey", min = 0, max = 60)
    public static int fhkBgMaxExtend = 18; // px

    @ColorPicker(key = "skyblock-fasthotkey", title = "Background Near Color")
    public static int fhkBgNearColor = new java.awt.Color(128,128,128,255).getRGB();
    @ColorPicker(key = "skyblock-fasthotkey", title = "Background Far Color")
    public static int fhkBgFarColor = new java.awt.Color(128,128,128,255).getRGB();

    // === Slayer ===
    @ToggleButton(
        key = "slayer-miniboss",
        name = "Slayer Miniboss ESP",
        description = "Outline nearby slayer miniboss mobs",
        category = "Slayer"
    )
    public static boolean enableSlayerMiniboss = false;

    // --- Dungeon Terminals Settings ---
    @ToggleButton(
        key = "dungeons-terminals",
        name = "Dungeon Terminals",
        description = "Custom GUI and solver for terminals",
        category = "Dungeon"
    )
    public static boolean enableDungeonTerminals = true;

    @CheckBox(
        key = "dungeons-terminals",
        title = "Smooth Terminal (High Ping Mode)"
    )
    public static boolean terminalHighPingMode = false;

    @Slider(
        key = "dungeons-terminals",
        min = 0,
        max = 3
    )
    public static float terminalScale = 1.0f;

    @Slider(
        key = "dungeons-terminals",
        min = 0,
        max = 2000
    )
    public static int terminalTimeoutMs = 500;

    @Slider(
        key = "dungeons-terminals",
        min = 0,
        max = 500
    )
    public static int terminalFirstClickMs = 0;

    @Slider(
        key = "dungeons-terminals",
        min = -500,
        max = 500
    )
    public static int terminalOffsetX = 0;

    @Slider(
        key = "dungeons-terminals",
        min = -500,
        max = 500
    )
    public static int terminalOffsetY = 0;

    @ColorPicker(
        key = "dungeons-terminals",
        title = "Overlay Color"
    )
    public static int terminalOverlayColor = 0x00FF00FF; // RGBA

    @ColorPicker(
        key = "dungeons-terminals",
        title = "Background Color"
    )
    public static int terminalBackgroundColor = 0x0000007F; // RGBA

    @Slider(
        key = "dungeons-terminals",
        min = 0,
        max = 20
    )
    public static int terminalCornerRadiusBg = 1;

    @Slider(
        key = "dungeons-terminals",
        min = 0,
        max = 20
    )
    public static int terminalCornerRadiusCell = 1;

    @Slider(
        key = "dungeons-terminals",
        min = 0,
        max = 1000
    )
    public static int terminalHighPingIntervalMs = 120;

    // Per-terminal toggles
    @CheckBox(
        key = "dungeons-terminals",
        title = "Enable Numbers Terminal"
    )
    public static boolean terminalEnableNumbers = true;

    @CheckBox(
        key = "dungeons-terminals",
        title = "Enable Starts With Terminal"
    )
    public static boolean terminalEnableStartsWith = true;

    @CheckBox(
        key = "dungeons-terminals",
        title = "Enable Colors Terminal"
    )
    public static boolean terminalEnableColors = true;

    @CheckBox(
        key = "dungeons-terminals",
        title = "Enable Red Green Terminal"
    )
    public static boolean terminalEnableRedGreen = true;

    @CheckBox(
        key = "dungeons-terminals",
        title = "Enable Rubix Terminal"
    )
    public static boolean terminalEnableRubix = true;

    @CheckBox(
        key = "dungeons-terminals",
        title = "Enable Melody Terminal"
    )
    public static boolean terminalEnableMelody = true;

    @ToggleButton(
            key = "kuudra-dynamic-pearl",
            name = "Dynamic Pearl",
            description = "Show pearl spot everywhere in kuudra",
            category = "Kuudra"
    )
    public static boolean enableDynamicPearl = false;

    @ToggleButton(
            key = "kuudra-check-no-pre",
            name = "Check No Pre",
            description = "Send message in party chat when no supply at your current pre spot",
            category = "Kuudra"
    )
    public static boolean enableCheckNoPre = false;

    @ToggleButton(
            key = "kuudra-crate-aura",
            name = "Crate Aura",
            description = "Automatically right click on supply",
            category = "Kuudra"
    )
    public static boolean enableCrateAura = false;

    @ToggleButton(
            key = "kuudra-crate-beacon-beam",
            name = "Crate Beam",
            description = "Highlight supplies position with beacon beam",
            category = "Kuudra"
    )
    public static boolean enableCrateBeaconBeam = false;

    @ToggleButton(
            key = "kuudra-box-crate",
            name = "Box Crate",
            description = "Highlight the hitbox of supply",
            category = "Kuudra"
    )
    public static boolean enableBoxCrate = false;

    @ToggleButton(
            key = "kuudra-fixed-pos-pearl",
            name = "Non-dynamic Pearl",
            description = "Fixed position of pearl lineups",
            category = "Kuudra"
    )
    public static boolean enableFixedPosPearl = false;

    @ToggleButton(
            key = "kuudra-builders-count",
            name = "Builders Count",
            description = "Show how many players are currently building during phase two",
            category = "Kuudra"
    )
    public static boolean enableBuildersCount = false;

    @ToggleButton(
            key = "kuudra-build-piles",
            name = "Build Piles",
            description = "Highlight all six build positions with beacon beam",
            category = "Kuudra"
    )
    public static boolean enableBuildPiles = false;
}

