package com.aftertime.ratallofyou;

import com.aftertime.ratallofyou.modules.SkyBlock.*;
import com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey.FastHotKey;
import com.aftertime.ratallofyou.modules.SkyBlock.FastHotKey.FastHotKeyGui;
import com.aftertime.ratallofyou.KeyBind.KeybindHandler;
import com.aftertime.ratallofyou.modules.dungeon.CustomLeapMenu.LeapMenu;
import com.aftertime.ratallofyou.modules.kuudra.*;
import com.aftertime.ratallofyou.modules.kuudra.PhaseOne.*;
import com.aftertime.ratallofyou.modules.kuudra.PhaseOne.PearlLineUp.CalcPearlLineUp;
import com.aftertime.ratallofyou.modules.kuudra.PhaseTwo.*;
import com.aftertime.ratallofyou.modules.render.*;
import com.aftertime.ratallofyou.modules.dungeon.*;
import com.aftertime.ratallofyou.modules.dungeon.StarMobHighlighter;
import com.aftertime.ratallofyou.modules.slayer.Miniboss;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.PartyUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import com.aftertime.ratallofyou.UI.config.ConfigIO;
import com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive.Direction;
import com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive.KuudraHitbox;
import com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive.KuudraHP;
import com.aftertime.ratallofyou.modules.Performance.HideUselessMessage;
import com.aftertime.ratallofyou.modules.Performance.HideLightning;
import com.aftertime.ratallofyou.modules.SkyBlock.StorageOverview.StorageOverviewModule;
import com.aftertime.ratallofyou.modules.Fishing.AutoFish;
import com.aftertime.ratallofyou.modules.render.PlayerESP; // Register Player ESP
import com.aftertime.ratallofyou.modules.render.CustomCape;
import com.aftertime.ratallofyou.modules.debugdata.DebugDataTicker;
import com.aftertime.ratallofyou.UI.newui.config.ModConfigIO; // Load new UI ModConfig

@Mod(modid = Main.MODID, version = Main.VERSION)
public class Main {
    public static final String MODID = "ratallofyou";
    public static final String VERSION = "2.3";
    public static Minecraft mc = Minecraft.getMinecraft();

    // Small helper to reduce repetitive register calls
    private static void registerAll(Object... listeners) {
        for (Object listener : listeners) {
            MinecraftForge.EVENT_BUS.register(listener);
        }
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize and load all configs using the new config system
        ConfigIO.INSTANCE.InitializeConfigs();
        // Load new UI ModConfig persisted values
        ModConfigIO.load();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        registerAll(
                // Kuudra utilities and phases
                new RefillPearls(), new InvincibleTimer(), new PearlCancel(), new CalcPearlLineUp(), new KuudraUtils(),
                new Direction(), new KuudraHitbox(), new KuudraHP(), new FixedPearlLineUp(), new CheckNoPre(),

                // Dungeon helpers
                new GoldorStartTimer(), new P3TickTimer(), new F7GhostBlocks(), new DungeonUtils(), new LeapAnnounce(),
                new KeyHighlighter(), new StarMobHighlighter(), new SecretClicks(), new NoDebuff(),
                new ChestOpenNotice(), new PosionArrow(), new WatcherClear(), new LeapMenu(),
                new HealerWishAlert(), new FindCorrectLivid(), new SalvageItem(), new AutoSell(),
                // Crate/building related
                new CrateHighlighter(), new CrateAura(), new CrateBeaconBeam(), new BuildPilesRenderer(),
                new FreshMessageHandler(), new BuildBuildersRenderer(), new BuildStandsTracker(), new CratePriority(),

                // SkyBlock QoL
                new AutoSprint(), new PartyUtils(), new ChatCommands(), new Fullbright(), new EtherwarpOverlay(),
                new PlayerESP(), new NameTag(), new CustomCape(),
                new FastHotKey(), new FastHotKeyGui(), new WaypointGrab(), new HotbarSwap(), new SearchBar(),
                new FluxFlareTimer(), new StorageOverviewModule(),
                new AutoFish(), new AutoExperiment(), new SuperPairs(), new MarkLocation(),
                // Performance tweaks
                new HideUselessMessage(), new HideLightning(), new BlockUselessPerk(), new DarkMode(), new NoHurtCam(),

                // Slayer
                new Miniboss(),

                // Input/keybind listeners
                new KeybindHandler()
        );

        KeybindHandler.registerKeybinds();
        DebugDataTicker.register(); // Register the debug ticker for periodic debug info
    }
}