package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class P3TickTimer {
    private int barrierTicks = 0;
    private boolean isTimerActive = false;
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onChat(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        if (!ModConfig.phase3ticktimerenabled) return;

        String message = event.message.getUnformattedText();

        if (message.contains("[BOSS] Goldor: Who dares trespass into my domain?") ||
                message.contains("[BOSS] Goldor: What do you think you are doing there!") {
            startTimer();
        }
        else if (message.matches("Party > (?:\\[.+\\])? ?(?:.+)?[ቾ⚒]?: (Bonzo|Phoenix) Procced!?(?: \\(3s\\))?")) {
            startTimer();
        }
        else if (message.contains("The Core entrance is opening!")) {
            resetTimer();
        }
    }

    @SubscribeEvent
    public void onPacketReceived(net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent event) {
        if (!ModConfig.phase3TimerEnabled || !isTimerActive || !(event.packet instanceof S32PacketConfirmTransaction)) return;

        barrierTicks--;
        if (barrierTicks <= 0) {
            barrierTicks = 60;
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (!ModConfig.phase3TimerEnabled || !isTimerActive || event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        String time = String.format("%.2f", barrierTicks / 20.0f);
        String formattedTime;

        if (barrierTicks >= 40) { // 2+ seconds
            formattedTime = EnumChatFormatting.GREEN + time;
        } else if (barrierTicks >= 20) { // 1-2 seconds
            formattedTime = EnumChatFormatting.YELLOW + time;
        } else { // <1 second
            formattedTime = EnumChatFormatting.RED + time;
        }

        int x = (int)(ModConfig.timerX * mc.displayWidth);
        int y = (int)(ModConfig.timerY * mc.displayHeight);

        mc.fontRendererObj.drawStringWithShadow(
                formattedTime,
                x,
                y,
                0xFFFFFF
        );
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        resetTimer();
    }

    private void startTimer() {
        barrierTicks = 60;
        isTimerActive = true;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[RatAllOfYou] " +
                        EnumChatFormatting.GREEN + "Phase 3 Timer Started"
        ));
    }

    private void resetTimer() {
        barrierTicks = 0;
        isTimerActive = false;
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new P3TickTimer());
    }
}
