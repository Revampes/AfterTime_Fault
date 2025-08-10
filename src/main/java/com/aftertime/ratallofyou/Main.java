package com.aftertime.ratallofyou;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.modules.dungeon.InvincibleTimer;
//import com.aftertime.ratallofyou.modules.dungeon.PreGhostBlock;
import com.aftertime.ratallofyou.modules.kuudra.PearlCancel;
import com.aftertime.ratallofyou.modules.kuudra.RefillPearls;
//import com.aftertime.ratallofyou.modules.dungeon.F7GhostBlocks;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;


@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {
    public static final String MODID = "ratallofyou";
    public static final String VERSION = "1.0";
    public static ModConfig config;
    public static Minecraft mc = Minecraft.getMinecraft();


    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        config = new ModConfig();
        MinecraftForge.EVENT_BUS.register(new RefillPearls());
        MinecraftForge.EVENT_BUS.register(new InvincibleTimer());
        MinecraftForge.EVENT_BUS.register(new PearlCancel());
//        MinecraftForge.EVENT_BUS.register(new F7GhostBlocks());
//        MinecraftForge.EVENT_BUS.register(new PreGhostBlock.DungeonListener());
    }
}