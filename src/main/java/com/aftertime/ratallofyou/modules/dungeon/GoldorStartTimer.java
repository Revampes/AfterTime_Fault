package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.ModConfig;
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
        if (!isModuleEnabled("Phase 3 Timer")) return;

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
        if (!isModuleEnabled("Goldor Start CountDown") || ticks <= 0 || event.phase != TickEvent.Phase.START) return;
        ticks--;

        String time = String.format("%.2f", ticks / 20.0f);
        mc.ingameGUI.displayTitle(EnumChatFormatting.GREEN + time, "", -1, -1, -1);

        if (ticks <= 0) {
            mc.ingameGUI.displayTitle("", "", -1, -1, -1);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        ticks = -1;
        mc.ingameGUI.displayTitle("", "", -1, -1, -1);
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new GoldorStartTimer());
    }

    private boolean isModuleEnabled(String moduleName) {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(moduleName)) {
                return module.enabled;
            }
        }
        return false;
    }
}
