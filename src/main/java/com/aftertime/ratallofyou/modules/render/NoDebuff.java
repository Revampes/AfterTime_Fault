package com.aftertime.ratallofyou.modules.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class NoDebuff {
    private static boolean enabled = false;
    private static boolean noBlindness = false;
    private static boolean noFire = false;
    private static boolean clearLiquidVision = false;

    // Setters that save config
    public static void setEnabled(boolean state) {
        enabled = state;
        saveConfig();
    }

    public static void setNoBlindness(boolean state) {
        noBlindness = state;
        saveConfig();
    }

    public static void setNoFire(boolean state) {
        noFire = state;
        saveConfig();
    }

    public static void setClearLiquidVision(boolean state) {
        clearLiquidVision = state;
        saveConfig();
    }

    // Getters
    public static boolean isEnabled() { return enabled; }
    public static boolean isNoBlindness() { return noBlindness; }
    public static boolean isNoFire() { return noFire; }
    public static boolean isClearLiquidVision() { return clearLiquidVision; }

    public static void loadConfig() {
        Properties props = new Properties();
        File configFile = new File("config/ratallofyou_nodebuff.cfg");

        try {
            if (configFile.exists()) {
                props.load(new FileInputStream(configFile));
                enabled = Boolean.parseBoolean(props.getProperty("enabled", "false"));
                noBlindness = Boolean.parseBoolean(props.getProperty("noBlindness", "false"));
                noFire = Boolean.parseBoolean(props.getProperty("noFire", "false"));
                clearLiquidVision = Boolean.parseBoolean(props.getProperty("clearLiquidVision", "false"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveConfig() {
        Properties props = new Properties();
        File configFile = new File("config/ratallofyou_nodebuff.cfg");

        try {
            // Load existing properties if file exists
            if (configFile.exists()) {
                props.load(new FileInputStream(configFile));
            }

            // Set all properties
            props.setProperty("enabled", String.valueOf(enabled));
            props.setProperty("noBlindness", String.valueOf(noBlindness));
            props.setProperty("noFire", String.valueOf(noFire));
            props.setProperty("clearLiquidVision", String.valueOf(clearLiquidVision));

            // Save to file
            props.store(new FileOutputStream(configFile), "NoDebuff Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
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