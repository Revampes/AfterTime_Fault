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

    // Test ColorPicker annotation
    @ColorPicker(
            key = "render-dark-mode",
            title = "Test"
    )
    public static int testColor = 0xFF000000; // Default black

    // Test DropdownBox annotation
    @DropdownBox(
            key = "render-dark-mode",
            title = "test",
            options = ""
    )
    public static int testDropdown = 0;

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
}