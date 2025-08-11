package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class InvincibleTimer {
    private int bonzoTime = 0;
    private int spiritTime = 0;
    private int phoenixTime = 0;
    private String procText = " ";
    private long procTextEndTime = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isModuleEnabled("Invincible Timer")) return;

        if (bonzoTime > 0) bonzoTime--;
        if (spiritTime > 0) spiritTime--;
        if (phoenixTime > 0) phoenixTime--;

        if (System.currentTimeMillis() > procTextEndTime) {
            procText = " ";
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isModuleEnabled("Invincible Timer")) return;

        String message = event.message.getUnformattedText();

        //bonzo
        if (message.contains("Bonzo's Mask saved your life")) {
            bonzoTime = 1800;
            procText = "§9Bonzo Mask Procced";
            procTextEndTime = System.currentTimeMillis() + 1500;
        }

        //spirit
        else if (message.contains("Spirit Mask saved your life")) {
            spiritTime = 300;
            procText = "§fSpirit Mask Procced";
            procTextEndTime = System.currentTimeMillis() + 1500;
        }

        //phoenix
        else if (message.contains("Phoenix Pet saved you")) {
            phoenixTime = 600;
            procText = "§cPhoenix Procced";
            procTextEndTime = System.currentTimeMillis() + 1500;
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !isModuleEnabled("Invincible Timer")) return;

        drawText("§9Bonzo: " + getStatusText(bonzoTime), 200, 200, 2);
        drawText("§fSpirit: " + getStatusText(spiritTime), 200, 218, 2);
        drawText("§cPhoenix: " + getStatusText(phoenixTime), 200, 236, 2);

        if (!procText.equals(" ")) {
            drawText(procText, 400, 300, 4);
        }
    }

    private String getStatusText(int time) {
        return time <= 0 ? "§aREADY" : "§6" + String.format("%.1f", time / 20f);
    }

    private void drawText(String text, int x, int y, float scale) {
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                text,
                x,
                y,
                0xFFFFFF
        );
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
