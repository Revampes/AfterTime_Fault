package com.aftertime.ratallofyou;

import com.aftertime.ratallofyou.UI.config.ConfigStorage;
import com.aftertime.ratallofyou.KeyBind.KeybindHandler;
import com.aftertime.ratallofyou.modules.kuudra.*;
import com.aftertime.ratallofyou.modules.render.EtherwarpOverlay;
import com.aftertime.ratallofyou.modules.render.Fullbright;
import com.aftertime.ratallofyou.modules.SkyBlock.AutoSprint;
import com.aftertime.ratallofyou.modules.SkyBlock.ChatCommands;
import com.aftertime.ratallofyou.modules.dungeon.*;
import com.aftertime.ratallofyou.UI.UIHighlighter;
import com.aftertime.ratallofyou.UI.UIDragger;
import com.aftertime.ratallofyou.modules.render.NoDebuff;
import com.aftertime.ratallofyou.modules.dungeon.StarMobHighlighter;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.PartyUtils;
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
        ConfigStorage.loadAllConfigs();
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
        MinecraftForge.EVENT_BUS.register(new UIHighlighter());
        MinecraftForge.EVENT_BUS.register(new F7GhostBlocks());
        MinecraftForge.EVENT_BUS.register(new DungeonUtils());
        MinecraftForge.EVENT_BUS.register(new LeapAnnounce());
        MinecraftForge.EVENT_BUS.register(new KeyHighlighter());
        MinecraftForge.EVENT_BUS.register(new StarMobHighlighter());
        MinecraftForge.EVENT_BUS.register(new CrateBeam());
        MinecraftForge.EVENT_BUS.register(new CrateHighlighter());
        MinecraftForge.EVENT_BUS.register(new PartyUtils());
        MinecraftForge.EVENT_BUS.register(new ChatCommands());
        MinecraftForge.EVENT_BUS.register(new Fullbright());
        MinecraftForge.EVENT_BUS.register(new SecretClicks());
        MinecraftForge.EVENT_BUS.register(new NoDebuff());
        MinecraftForge.EVENT_BUS.register(new PearlLineups());
        MinecraftForge.EVENT_BUS.register(new KuudraUtils());
        MinecraftForge.EVENT_BUS.register(new EtherwarpOverlay());

        // Register keybind handler
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
        KeybindHandler.registerKeybinds();
        // Load UI positions
        UIDragger.getInstance().loadPositions();
    }
}