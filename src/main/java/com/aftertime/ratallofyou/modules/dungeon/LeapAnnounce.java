package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.settings.BooleanSetting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LeapAnnounce {
    private static final BooleanSetting MODULE_ENABLED = new BooleanSetting("Leap Announce");
    private final Minecraft mc = Minecraft.getMinecraft();

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new LeapAnnounce());
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isModuleEnabled() || event.type != 0) return; // Only process regular chat messages

        String message = event.message.getUnformattedText();

        // Handle personal leap message
        if (message.startsWith("You have teleported to ") && message.endsWith("!")) {
            String name = message.substring("You have teleported to ".length(), message.length() - 1);
            sendPartyChat("Leaped to " + name + "!");
            return;
        }

        // Handle party leap messages
        if (message.startsWith("Party > ")) {
            String[] parts = message.split(" ");
            if (parts.length < 5 || !message.contains("Leaped to")) return;

            String from = parts[2].replaceAll("[ቾ⚒:]", "").trim();
            String to = parts[parts.length - 1].replace("!", "").trim();

            // Always show leap messages (no filtering)
            // Previous config-based filtering removed
        }
    }

    private void sendPartyChat(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.sendChatMessage("/pc " + message);
        }
    }

    private boolean isModuleEnabled() {
        return MODULE_ENABLED.isEnabled();
    }
}