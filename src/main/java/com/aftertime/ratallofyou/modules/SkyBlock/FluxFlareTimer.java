package com.aftertime.ratallofyou.modules.SkyBlock;

import com.aftertime.ratallofyou.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class FluxFlareTimer {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private String hudText = "";
    private long flareExpireMs = 0L;
    private double flareX = 0, flareY = 0, flareZ = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isEnabled()) { hudText = ""; return; }
        if (mc.theWorld == null || mc.thePlayer == null) { hudText = ""; return; }

        double bestDistance = Double.MAX_VALUE;
        String bestLabel = "";

        long now = System.currentTimeMillis();

        // Scan entities for Flux (armor stands name) and Firework flares
        for (Object o : mc.theWorld.loadedEntityList) {
            if (!(o instanceof Entity)) continue;
            Entity e = (Entity) o;

            // Detect flare rocket by class/name; record location and start/refresh a 180s countdown
            String simple = e.getClass().getSimpleName().toLowerCase(java.util.Locale.ROOT);
            String dispName = null;
            try {
                dispName = e.getDisplayName() != null ? e.getDisplayName().getUnformattedText() : e.getName();
            } catch (Throwable ignore) { dispName = e.getName(); }

            if (simple.contains("firework") || (dispName != null && dispName.toLowerCase(java.util.Locale.ROOT).contains("firework"))) {
                flareExpireMs = now + 180_000L; // 180 seconds
                flareX = e.posX; flareY = e.posY; flareZ = e.posZ;
                continue;
            }

            // Flux names are on armor stand name tags; use display name
            if (!(e instanceof EntityArmorStand)) continue;
            String name = dispName != null ? dispName : "";
            if (name.isEmpty()) continue;

            boolean isFlux = name.contains("Radiant") || name.contains("Mana Flux") || name.contains("Overflux") || name.contains("Plasmaflux");
            if (!isFlux) continue;

            double dist = mc.thePlayer.getDistanceToEntity(e);
            double limit = name.contains("Plasmaflux") ? 20.0 : 18.0;
            if (dist <= limit && dist < bestDistance) {
                bestDistance = dist;
                bestLabel = name;
            }
        }

        // Consider active flare countdown if closer than any found flux and within 40 blocks
        if (flareExpireMs > now) {
            int remain = (int) Math.ceil((flareExpireMs - now) / 1000.0);
            double dx = mc.thePlayer.posX - flareX;
            double dy = mc.thePlayer.posY - flareY;
            double dz = mc.thePlayer.posZ - flareZ;
            double flareDist = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (flareDist < 40.0 && flareDist < bestDistance) {
                bestLabel = "\u00a7cFlare \u00a7e" + remain + "s";
                bestDistance = flareDist;
            }
        }

        hudText = bestLabel;

        // Expire flare tracking if time passed
        if (flareExpireMs > 0 && flareExpireMs <= now) {
            flareExpireMs = 0L;
            flareX = flareY = flareZ = 0.0;
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (!isEnabled()) return;
        if (hudText == null || hudText.isEmpty()) return;

        float s = getScale();

        ScaledResolution sr = new ScaledResolution(mc);
        int x = Math.max(0, Math.min(ModConfig.flarefluxX, sr.getScaledWidth() - 2));
        int y = Math.max(0, Math.min(ModConfig.flarefluxY, sr.getScaledHeight() - 2));

        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        try {
            net.minecraft.client.renderer.GlStateManager.translate(x, y, 0);
            net.minecraft.client.renderer.GlStateManager.scale(s, s, 1.0f);
            mc.fontRendererObj.drawString(hudText, 0, 0, 0xFFFFFF, true);
        } finally {
            net.minecraft.client.renderer.GlStateManager.popMatrix();
        }
    }

    private boolean isEnabled() {
        return ModConfig.enableFluxFlareTimer;
    }

    private float getScale() {
        float v = ModConfig.flarefluxScale;
        return v <= 0 ? 1.0f : v;
    }
}
