package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class P3TickTimer {
    private int barrierTicks = 0;
    private boolean isTimerActive = false;
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onChat(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        if (!isModuleEnabled() || !DungeonUtils.isInDungeon()) return;

        // Basic start/stop logic bound to phase
        if (DungeonUtils.isInP3()) {
            startTimer();
        } else {
            resetTimer();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!isModuleEnabled() || !isTimerActive || event.phase != TickEvent.Phase.START) return;
        barrierTicks--;
        if (barrierTicks <= 0) {
            barrierTicks = 60; // 3 seconds (60 ticks)
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (!isModuleEnabled() || !isTimerActive || event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        // Read position/scale from ModConfig
        int x = ModConfig.p3ticktimerX;
        int y = ModConfig.p3ticktimerY;
        float scale = ModConfig.p3ticktimerScale <= 0 ? 1.0f : ModConfig.p3ticktimerScale;

        // Fallback to center if uninitialized
        if (x == 0 && y == 0) {
            ScaledResolution res = new ScaledResolution(mc);
            x = res.getScaledWidth() / 2;
            y = res.getScaledHeight() / 2;
        }

        String time = String.format("%.2f", barrierTicks / 20.0f);
        String formattedTime = getFormattedTime(time);

        int textWidth = mc.fontRendererObj.getStringWidth(formattedTime);
        int renderX = x - Math.round((textWidth * scale) / 2f);
        int renderY = y;

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0f);
        mc.fontRendererObj.drawStringWithShadow(formattedTime, (int) (renderX / scale), (int) (renderY / scale), 0xFFFFFF);
        GlStateManager.popMatrix();
    }

    private String getFormattedTime(String time) {
        if (barrierTicks >= 40) return EnumChatFormatting.GREEN + time;
        if (barrierTicks >= 20) return EnumChatFormatting.YELLOW + time;
        return EnumChatFormatting.RED + time;
    }

    private void startTimer() {
        barrierTicks = 60;
        isTimerActive = true;
        mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GOLD + "[RatAllOfYou] " + EnumChatFormatting.GREEN + "Phase 3 Timer Started"
        ));
    }

    private void resetTimer() {
        barrierTicks = 0;
        isTimerActive = false;
    }

    private boolean isModuleEnabled() {
        return ModConfig.enableP3TickTimer;
    }
}
