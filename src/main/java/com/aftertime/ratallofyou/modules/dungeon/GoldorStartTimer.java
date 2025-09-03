package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GoldorStartTimer {

    private int ticks = -1;
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!BooleanSettings.isEnabled("dungeons_phase3countdown")) return;

        String message = event.message.getUnformattedText();

        if (message.contains("[BOSS] Storm: I should have known that I stood no chance.")) {
            ticks = 104; // 104 ticks = 5.2 seconds
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.GOLD + "[RatAllOfYou] " +
                    EnumChatFormatting.GREEN + "Phase 3 Timer Started!"
            ));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!BooleanSettings.isEnabled("dungeons_phase3countdown") || ticks <= 0 || event.phase != TickEvent.Phase.START) return;
        ticks--;

        String time = String.format("%.2f", ticks / 20.0f);
        DungeonUtils.sendTitle(EnumChatFormatting.GREEN + time, "", -1, -1, -1);

        if (ticks <= 0) {
            DungeonUtils.clearTitle();
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        ticks = -1;
        DungeonUtils.clearTitle();
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new GoldorStartTimer());
    }
}
