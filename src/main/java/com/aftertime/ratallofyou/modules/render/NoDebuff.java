package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
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
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("render_nodebuff");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
    private static boolean noBlindness() {
        Boolean b = (Boolean) AllConfig.INSTANCE.NODEBUFF_CONFIGS.get("nodebuff_ignore_blindness").Data;
        return Boolean.TRUE.equals(b);
    }
    private static boolean noFire() {
        Boolean b = (Boolean) AllConfig.INSTANCE.NODEBUFF_CONFIGS.get("nodebuff_remove_fire_overlay").Data;
        return Boolean.TRUE.equals(b);
    }
    private static boolean clearLiquidVision() {
        Boolean b = (Boolean) AllConfig.INSTANCE.NODEBUFF_CONFIGS.get("nodebuff_clear_liquid_vision").Data;
        return Boolean.TRUE.equals(b);
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