package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;

import com.aftertime.ratallofyou.UI.config.ConfigData.UIPosition;
import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
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
            procText = "\u00a79Bonzo Mask Procced";
            procTextEndTime = System.currentTimeMillis() + 1500;
        }
        else if (message.contains("Spirit Mask saved your life")) {
            spiritTime = 300;
            procText = "\u00a7fSpirit Mask Procced";
            procTextEndTime = System.currentTimeMillis() + 1500;
        }
        else if (message.contains("Phoenix Pet saved you")) {
            phoenixTime = 600;
            procText = "\u00a7cPhoenix Procced";
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

        float scale = 1.0f;
        Object sc = AllConfig.INSTANCE.Pos_CONFIGS.get("invincible_scale").Data;
        if (sc instanceof Float) scale = (Float) sc; else if (sc instanceof Double) scale = ((Double) sc).floatValue();

        drawScaledText("\u00a79Bonzo: " + getStatusText(bonzoTime), bonzoPos.x, bonzoPos.y, scale);
        drawScaledText("\u00a7fSpirit: " + getStatusText(spiritTime), spiritPos.x, spiritPos.y, scale);
        drawScaledText("\u00a7cPhoenix: " + getStatusText(phoenixTime), phoenixPos.x, phoenixPos.y, scale);

        if (!procText.equals(" ")) {
            drawScaledText(procText, procPos.x, procPos.y, scale);
        }
    }

    private String getStatusText(int time) {
        return time <= 0 ? "\u00a7aREADY" : "\u00a76" + String.format("%.1f", time / 20f);
    }

    private void drawScaledText(String text, int x, int y, float scale) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0f);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(
                text,
                (int)(x / scale),
                (int)(y / scale),
                0xFFFFFF
        );
        GlStateManager.popMatrix();
    }

    private boolean isModuleEnabled() {
        return BooleanSettings.isEnabled("dungeons_invincibletimer");
    }
}