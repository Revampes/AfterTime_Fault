package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.UI.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Properties;

public class NoDebuff {
    private static boolean enabled = false;
    private static boolean noBlindness = false;
    private static boolean noFire = false;
    private static boolean clearLiquidVision = false;

    // Setters that save config
    public static void setEnabled(boolean state) {
        enabled = state;
        ModConfig.saveConfig();
    }

    public static void setNoBlindness(boolean state) {
        noBlindness = state;
        ModConfig.saveConfig();
    }

    public static void setNoFire(boolean state) {
        noFire = state;
        ModConfig.saveConfig();
    }

    public static void setClearLiquidVision(boolean state) {
        clearLiquidVision = state;
        ModConfig.saveConfig();
    }

    // Getters
    public static boolean isEnabled() { return enabled; }
    public static boolean isNoBlindness() { return noBlindness; }
    public static boolean isNoFire() { return noFire; }
    public static boolean isClearLiquidVision() { return clearLiquidVision; }

    public static void loadConfig(Properties props) {
        noBlindness = Boolean.parseBoolean(props.getProperty("nodebuff_noblindness", "false"));
        noFire = Boolean.parseBoolean(props.getProperty("nodebuff_nofire", "false"));
        clearLiquidVision = Boolean.parseBoolean(props.getProperty("nodebuff_clearliquidvision", "false"));
    }

    @SubscribeEvent
    public void onRenderFog(EntityViewRenderEvent.FogDensity event) {
        if (!enabled) return;

        if (noBlindness) {
            event.density = 0f;
            event.setCanceled(true);
            GlStateManager.setFogStart(998f);
            GlStateManager.setFogEnd(999f);
        }

        if (clearLiquidVision && (event.entity.isInWater() || event.entity.isInLava())) {
            event.density = 0.1f;
            event.setCanceled(true);
            GlStateManager.setFogStart(998f);
            GlStateManager.setFogEnd(999f);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (!enabled) return;

        if (noBlindness && event.type == RenderGameOverlayEvent.ElementType.HELMET) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockOverlay(RenderBlockOverlayEvent event) {
        if (!enabled) return;

        if (noFire && event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        }
    }
}