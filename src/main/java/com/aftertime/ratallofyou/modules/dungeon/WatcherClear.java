package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class WatcherClear {

    private static final long TARGET_DELAY_MS = 22_000L; // 22 seconds
    private static final int COUNTDOWN_SECONDS = 3;

    private final Minecraft mc = Minecraft.getMinecraft();

    private boolean bloodOpen = false;
    private boolean done = false; // ensure sequence runs once per world
    private long countdownStartAt = -1L; // timestamp when to start 3..2..1
    private int countdownLeft = -1; // seconds left in countdown, -1 = inactive
    private long nextTickAt = -1L; // next timestamp to update countdown

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!BooleanSettings.isEnabled("dungeons_watcherclear") || done || bloodOpen || event == null || event.message == null) return;

        String msg = event.message.getUnformattedText();
        if (msg == null) return;

        // Match any Watcher line
        if (msg.contains("The Watcher:")) {
            bloodOpen = true;
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GOLD + "[RatAllOfYou] " + EnumChatFormatting.RED + "Blood Opened."));
            }
            long now = System.currentTimeMillis();
            countdownStartAt = now + TARGET_DELAY_MS;
            countdownLeft = -1;
            nextTickAt = -1;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!BooleanSettings.isEnabled("dungeons_watcherclear") || event.phase != TickEvent.Phase.START) return;

        long now = System.currentTimeMillis();

        // Waiting phase -> start countdown when delay passes
        if (bloodOpen && countdownLeft < 0 && countdownStartAt > 0 && now >= countdownStartAt) {
            countdownLeft = COUNTDOWN_SECONDS;
            nextTickAt = now; // show immediately on next line
        }

        // Handle countdown display at 1Hz
        if (countdownLeft >= 0 && now >= nextTickAt) {
            DungeonUtils.sendTitle(EnumChatFormatting.GREEN + String.valueOf(countdownLeft), "", 0, 20, 0);
            countdownLeft--;
            nextTickAt = now + 1000L;

            // When we hit below 0, show 0 then the final message
            if (countdownLeft < 0) {
                DungeonUtils.sendTitle(EnumChatFormatting.GREEN + "0", "", 0, 20, 0);
                showKillMobsMessage();
                // End state; don't re-trigger until world unload
                resetTitlesSoon();
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload e) {
        bloodOpen = false;
        done = false;
        countdownStartAt = -1L;
        countdownLeft = -1;
        nextTickAt = -1L;
        // Clear any lingering title
        DungeonUtils.clearTitle();
    }

    private void showKillMobsMessage() {
        if (mc.thePlayer == null) return;
        mc.thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.GOLD + "Kill Mobs"));
        DungeonUtils.sendTitle(EnumChatFormatting.RED.toString() + EnumChatFormatting.BOLD + "Kill Blood Mobs", "", 0, 30, 0);
        // Play a twinkle sound as cue
//        mc.thePlayer.playSound("fireworks.twinkle", 1.0f, 1.0f);
        // Mark sequence done and prevent retrigger
        done = true;
        bloodOpen = false;
    }

    private void resetTitlesSoon() {
        // Let the final title linger for its stay time; clearing will happen naturally on next screens
        // No-op; callers clear on world unload or next titles
    }
}
