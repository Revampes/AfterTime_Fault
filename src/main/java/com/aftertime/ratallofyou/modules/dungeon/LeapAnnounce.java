package com.aftertime.ratallofyou.modules.dungeon;


import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LeapAnnounce {
    private final Minecraft mc = Minecraft.getMinecraft();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new LeapAnnounce());
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!BooleanSettings.isEnabled("dungeons_leapannounce") || event.type != 0) return; // Only process regular chat messages

        String message = event.message.getUnformattedText();

        // Handle personal leap message
        if (message.startsWith("You have teleported to ") && message.endsWith("!")) {
            String name = message.substring("You have teleported to ".length(), message.length() - 1);
            sendPartyChat("Leaped to " + name + "!");
            return;
        }

        // Handle party leap messages (kept for future extension)
        if (message.startsWith("Party > ") && message.contains("Leaped to")) {
            // no-op
        }
    }

    private void sendPartyChat(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.sendChatMessage("/pc " + message);
        }
    }
}