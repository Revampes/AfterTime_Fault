package com.aftertime.ratallofyou;

import com.aftertime.ratallofyou.KeyBind.KeybindHandler;
import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.config.ModSettingsGui;
import com.aftertime.ratallofyou.modules.SkyBlock.AutoSprint;
import com.aftertime.ratallofyou.modules.dungeon.GoldorStartTimer;
import com.aftertime.ratallofyou.modules.dungeon.InvincibleTimer;
import com.aftertime.ratallofyou.modules.dungeon.P3TickTimer;
import com.aftertime.ratallofyou.modules.kuudra.PearlCancel;
import com.aftertime.ratallofyou.modules.kuudra.RefillPearls;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {
    public static final String MODID = "ratallofyou";
    public static final String VERSION = "1.0";
    public static Minecraft mc = Minecraft.getMinecraft();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.loadConfig(); // Load config before modules register
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Register modules
        MinecraftForge.EVENT_BUS.register(new RefillPearls());
        MinecraftForge.EVENT_BUS.register(new InvincibleTimer());
        MinecraftForge.EVENT_BUS.register(new PearlCancel());
        MinecraftForge.EVENT_BUS.register(new AutoSprint());
        MinecraftForge.EVENT_BUS.register(new GoldorStartTimer());
        MinecraftForge.EVENT_BUS.register(new P3TickTimer());

        // Register keybind handler
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
        KeybindHandler.registerKeybinds();
    }
}