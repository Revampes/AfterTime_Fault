package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.UIDragger;
import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;

import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
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

    // Initialize positions
    public InvincibleTimer() {
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !isModuleEnabled()) return;

        if (bonzoTime > 0) bonzoTime--;
        if (spiritTime > 0) spiritTime--;
        if (phoenixTime > 0) phoenixTime--;

        if (System.currentTimeMillis() > procTextEndTime) {
            procText = " ";
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isModuleEnabled()) return;

        String message = event.message.getUnformattedText();

        if (message.contains("Bonzo's Mask saved your life")) {
            bonzoTime = 1800;
            procText = "§9Bonzo Mask Procced";
            procTextEndTime = System.currentTimeMillis() + 1500;
        }
        else if (message.contains("Spirit Mask saved your life")) {
            spiritTime = 300;
            procText = "§fSpirit Mask Procced";
            procTextEndTime = System.currentTimeMillis() + 1500;
        }
        else if (message.contains("Phoenix Pet saved you")) {
            phoenixTime = 600;
            procText = "§cPhoenix Procced";
            procTextEndTime = System.currentTimeMillis() + 1500;
        }
    }
    UIPosition bonzoPos = (UIPosition) AllConfig.INSTANCE.Pos_CONFIGS.get("bonzo_pos").Data;
    UIPosition spiritPos = (UIPosition) AllConfig.INSTANCE.Pos_CONFIGS.get("spirit_pos").Data;
    UIPosition phoenixPos = (UIPosition) AllConfig.INSTANCE.Pos_CONFIGS.get("phoenix_pos").Data;
    UIPosition procPos = (UIPosition) AllConfig.INSTANCE.Pos_CONFIGS.get("proc_pos").Data;
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT || !isModuleEnabled()) return;

        // Get positions from UIDraggers


        // Draw using draggable positions
        drawText("§9Bonzo: " + getStatusText(bonzoTime), bonzoPos.x, bonzoPos.y, 2);
        drawText("§fSpirit: " + getStatusText(spiritTime), spiritPos.x, spiritPos.y, 2);
        drawText("§cPhoenix: " + getStatusText(phoenixTime), phoenixPos.x, phoenixPos.y, 2);

        if (!procText.equals(" ")) {
            drawText(procText, procPos.x, procPos.y, 4);
        }
    }

    private String getStatusText(int time) {
        return time <= 0 ? "§aREADY" : "§6" + String.format("%.1f", time / 20f);
    }

    private void drawText(String text, int x, int y, float scale) {
        // Scale implementation if needed
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                text,
                x,
                y,
                0xFFFFFF
        );
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("dungeons_invincibletimer");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}