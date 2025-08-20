package com.aftertime.ratallofyou.modules.kuudra;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CrateBeam {
    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isModuleEnabled() || mc.theWorld == null || mc.thePlayer == null) return;

        final float partialTicks = event.partialTicks;
        final double maxRange = 512.0; // render much farther crates
        final float beamHeight = 80.0f; // taller beam for visibility

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!KuudraUtils.isValidSupply(entity)) continue;

            double distance = mc.thePlayer.getDistanceToEntity(entity);
            if (distance > maxRange) continue;

            // Decide anchor: prefer the carrier (zombie) if it's attached/nearby for smooth tracking while pulling
            Vec3 anchor = pickBestAnchor(entity, partialTicks);

            // Distance-based alpha with sensible floor
            float alphaF = (float) (1.0 - (distance / 600.0));
            alphaF = Math.max(0.3f, Math.min(1.0f, alphaF));
            int alpha = (int) (alphaF * 255);

            RenderUtils.Color beamColor = new RenderUtils.Color(0, 255, 0, alpha);

            // Render beacon beam at interpolated position; disable depth so it shows through walls
            RenderUtils.renderBeaconBeam(
                    anchor,
                    beamColor,
                    false, // no depth check: always visible
                    beamHeight,
                    partialTicks
            );
        }
    }

    private Vec3 pickBestAnchor(Entity armorStand, float partialTicks) {
        // Fallback to the stand position
        double sx = armorStand.lastTickPosX + (armorStand.posX - armorStand.lastTickPosX) * partialTicks;
        double sy = armorStand.lastTickPosY + (armorStand.posY - armorStand.lastTickPosY) * partialTicks;
        double sz = armorStand.lastTickPosZ + (armorStand.posZ - armorStand.lastTickPosZ) * partialTicks;
        Vec3 standPos = new Vec3(sx, sy, sz);

        // Find a nearby zombie likely carrying the crate; crates are often attached to a zombie with no helmet
        EntityZombie best = null;
        double bestDistSq = 4.0; // 2 blocks radius squared
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityZombie)) continue;
            double dx = e.posX - armorStand.posX;
            double dy = e.posY - armorStand.posY;
            double dz = e.posZ - armorStand.posZ;
            double dSq = dx*dx + dy*dy + dz*dz;
            if (dSq < bestDistSq) {
                bestDistSq = dSq;
                best = (EntityZombie) e;
            }
        }

        if (best != null) {
            double ix = best.lastTickPosX + (best.posX - best.lastTickPosX) * partialTicks;
            double iy = best.lastTickPosY + (best.posY - best.lastTickPosY) * partialTicks;
            double iz = best.lastTickPosZ + (best.posZ - best.lastTickPosZ) * partialTicks;
            return new Vec3(ix, iy, iz);
        }

        return standPos;
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_cratebeam");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}