package com.aftertime.ratallofyou;

import com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey.FastHotKey;
import com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey.FastHotKeyGui;
import com.aftertime.ratallofyou.KeyBind.KeybindHandler;
import com.aftertime.ratallofyou.modules.SkyBlock.WaypointGrab;
import com.aftertime.ratallofyou.modules.dungeon.terminals.startswith;
import com.aftertime.ratallofyou.modules.kuudra.*;
import com.aftertime.ratallofyou.modules.kuudra.PhaseOne.*;
import com.aftertime.ratallofyou.modules.kuudra.PhaseOne.PearlLineUp.CalcPearlLineUp;
import com.aftertime.ratallofyou.modules.kuudra.PhaseTwo.*;
import com.aftertime.ratallofyou.modules.render.*;
import com.aftertime.ratallofyou.modules.SkyBlock.AutoSprint;
import com.aftertime.ratallofyou.modules.SkyBlock.ChatCommands;
import com.aftertime.ratallofyou.modules.dungeon.*;
import com.aftertime.ratallofyou.UI.UIHighlighter;
import com.aftertime.ratallofyou.modules.dungeon.StarMobHighlighter;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.PartyUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.modules.dungeon.terminals.TerminalSettingsApplier;
import com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive.Direction;
import com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive.KuudraHitbox;
import com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive.KuudraHP;
import com.aftertime.ratallofyou.modules.SkyBlock.HotbarSwap;
import com.aftertime.ratallofyou.modules.Performance.HideUselessMessage;
import com.aftertime.ratallofyou.modules.Performance.HideLightning;
import com.aftertime.ratallofyou.modules.SkyBlock.SearchBar;
import com.aftertime.ratallofyou.modules.SkyBlock.FluxFlareTimer;

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {
    public static final String MODID = "ratallofyou";
    public static final String VERSION = "1.0";
    public static Minecraft mc = Minecraft.getMinecraft();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize and load all configs using the new config system
        ConfigIO.INSTANCE.InitializeConfigs();
        // Apply terminal settings (defaults + per-terminal toggles)
        TerminalSettingsApplier.applyFromAllConfig();
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
        MinecraftForge.EVENT_BUS.register(new CrateHighlighter());
        MinecraftForge.EVENT_BUS.register(new CrateAura());
        MinecraftForge.EVENT_BUS.register(new PartyUtils());
        MinecraftForge.EVENT_BUS.register(new ChatCommands());
        MinecraftForge.EVENT_BUS.register(new Fullbright());
        MinecraftForge.EVENT_BUS.register(new SecretClicks());
        MinecraftForge.EVENT_BUS.register(new NoDebuff());
        MinecraftForge.EVENT_BUS.register(new CalcPearlLineUp());
        MinecraftForge.EVENT_BUS.register(new KuudraUtils());
        MinecraftForge.EVENT_BUS.register(new EtherwarpOverlay());
        MinecraftForge.EVENT_BUS.register(new FastHotKey());
        MinecraftForge.EVENT_BUS.register(new FastHotKeyGui());
        MinecraftForge.EVENT_BUS.register(new startswith());
        MinecraftForge.EVENT_BUS.register(new CrateBeaconBeam());
        MinecraftForge.EVENT_BUS.register(new CheckNoPre());
        MinecraftForge.EVENT_BUS.register(new WaypointGrab());
        MinecraftForge.EVENT_BUS.register(new BuildPilesRenderer());
        MinecraftForge.EVENT_BUS.register(new FreshMessageHandler());
        MinecraftForge.EVENT_BUS.register(new BuildBuildersRenderer());
        MinecraftForge.EVENT_BUS.register(new BuildStandsTracker());
        MinecraftForge.EVENT_BUS.register(new Direction());
        MinecraftForge.EVENT_BUS.register(new KuudraHitbox());
        MinecraftForge.EVENT_BUS.register(new KuudraHP());
        MinecraftForge.EVENT_BUS.register(new FixedPearlLineUp());
        MinecraftForge.EVENT_BUS.register(new KeybindHandler());
        MinecraftForge.EVENT_BUS.register(new ChestOpenNotice());
        MinecraftForge.EVENT_BUS.register(new HotbarSwap());
        MinecraftForge.EVENT_BUS.register(new HideUselessMessage());
        MinecraftForge.EVENT_BUS.register(new HideLightning());
        MinecraftForge.EVENT_BUS.register(new BlockUselessPerk());
        MinecraftForge.EVENT_BUS.register(new SearchBar());
        // New: Arrow Poison Tracker HUD
        MinecraftForge.EVENT_BUS.register(new PosionArrow());
        // New: Watcher Clear notifier
        MinecraftForge.EVENT_BUS.register(new WatcherClear());
        // New: Flare/Flux Timer HUD
        MinecraftForge.EVENT_BUS.register(new FluxFlareTimer());
        // New: Crate Priority title helper
        MinecraftForge.EVENT_BUS.register(new CratePriority());
        KeybindHandler.registerKeybinds();
    }
}