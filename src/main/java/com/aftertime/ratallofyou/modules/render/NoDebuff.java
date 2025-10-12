package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.config.ModConfig;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoDebuff {
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

    private static boolean isEnabled() {
        return ModConfig.enableNoDebuff;
    }
    private static boolean noBlindness() {
        return ModConfig.nodebuffIgnoreBlindness;
    }
    private static boolean noFire() {
        return ModConfig.nodebuffRemoveFireOverlay;
    }
    private static boolean clearLiquidVision() {
        return ModConfig.nodebuffClearLiquidVision;
    }

    @SubscribeEvent
    public void onRenderFog(EntityViewRenderEvent.FogDensity event) {
        if (!isEnabled()) return;

        if (noBlindness()) {
            event.density = 0f;
            event.setCanceled(true);
            GlStateManager.setFogStart(998f);
            GlStateManager.setFogEnd(999f);
        }

        if (clearLiquidVision() && (event.entity.isInWater() || event.entity.isInLava())) {
            event.density = 0.1f;
            event.setCanceled(true);
            GlStateManager.setFogStart(998f);
            GlStateManager.setFogEnd(999f);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (!isEnabled()) return;

        if (noBlindness() && event.type == RenderGameOverlayEvent.ElementType.HELMET) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockOverlay(RenderBlockOverlayEvent event) {
        if (!isEnabled()) return;

        if (noFire() && event.overlayType == RenderBlockOverlayEvent.OverlayType.FIRE) {
            event.setCanceled(true);
        }
    }
}