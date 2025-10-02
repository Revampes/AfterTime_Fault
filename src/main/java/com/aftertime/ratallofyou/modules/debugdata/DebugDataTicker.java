package com.aftertime.ratallofyou.modules.debugdata;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class DebugDataTicker {
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 200;
    private static boolean registered = false;

    public static void register() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(new DebugDataTicker());
            registered = true;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        tickCounter++;
        if (tickCounter >= TICK_INTERVAL) {
            tickCounter = 0;
            if (GetScoreboardDetails.isEnabled()) {
                GetScoreboardDetails.printScoreboardDetails();
            }
            if (GetTablistDetails.isEnabled()) {
                GetTablistDetails.printTablistDetails();
            }
        }
    }
}

