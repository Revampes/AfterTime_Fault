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
}