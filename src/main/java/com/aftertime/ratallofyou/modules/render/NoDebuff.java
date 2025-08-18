package com.aftertime.ratallofyou.modules.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoDebuff {
    private static boolean enabled = false;
    private static boolean noBlindness = false;
    private static boolean noFire = false;
    private static boolean clearLiquidVision = false;

    // Event handler instance & registration state
    private static NoDebuff handlerInstance;
    private static boolean registered = false;

    private static void ensureRegistered() {
        if (!registered) {
            if (handlerInstance == null) handlerInstance = new NoDebuff();
            MinecraftForge.EVENT_BUS.register(handlerInstance);
            registered = true;
        }
    }

    private static void ensureUnregistered() {
        if (registered && handlerInstance != null) {
            MinecraftForge.EVENT_BUS.unregister(handlerInstance);
            registered = false;
        }
    }

    // Setters (no direct file I/O; persistence handled by ConfigStorage)
    public static void setEnabled(boolean state) {
        enabled = state;
        if (enabled) ensureRegistered(); else ensureUnregistered();
    }

    public static void setNoBlindness(boolean state) { noBlindness = state; }
    public static void setNoFire(boolean state) { noFire = state; }
    public static void setClearLiquidVision(boolean state) { clearLiquidVision = state; }

    // Getters
    public static boolean isEnabled() { return enabled; }
    public static boolean isNoBlindness() { return noBlindness; }
    public static boolean isNoFire() { return noFire; }
    public static boolean isClearLiquidVision() { return clearLiquidVision; }


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