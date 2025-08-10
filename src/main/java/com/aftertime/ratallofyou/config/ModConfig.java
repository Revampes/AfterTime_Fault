package com.aftertime.ratallofyou.config;

import cc.polyfrost.oneconfig.config.Config;
import cc.polyfrost.oneconfig.config.annotations.Switch;
import cc.polyfrost.oneconfig.config.data.Mod;
import cc.polyfrost.oneconfig.config.data.ModType;

public class ModConfig extends Config {
    @Switch(
            name = "Enable Pearl Refill (Use at your own risk)",
            description = "Refill ender pearls YES!",
            category = "Kuudra"
    )
    public static boolean pearlRefill = false;

    @Switch(
            name = "Enable Invincible Timer (Currently not allow to move pos)",
            description = "Show a timer for invincibility phases in dungeons",
            category = "Dungeons"
    )
    public static boolean invincibleTimerEnabled = false;

//    @Switch(
//            name = "Enable PreGhost-Block",
//            description = "?",
//            category = "Dungeons"
//    )
//    public static boolean preGhostBlockEnabled = false;
//
//    @Switch(
//            name = "Enable Ghost Blocks Debug",
//            description = "Show debug messages for ghost blocks functionality",
//            category = "Dungeons"
//    )
//    public static boolean ghostBlocksDebugEnabled = false;

    @Switch(
            name = "Cancel Ender Pearl Interactions",
            description = "Prevents using ender pearls on blocks",
            category = "Kuudra"
    )
    public static boolean pearlCancel = false;

    public ModConfig() {
        super(new Mod("Rate All Of You", ModType.SKYBLOCK), "ratallofyou.json");
        initialize();
    }
}