package com.aftertime.ratallofyou.modules.kuudra;

import com.aftertime.ratallofyou.UI.ModConfig;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CrateBeam {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final String MODULE_NAME = "Crate Beam";

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!isEnabled() || mc.theWorld == null) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (KuudraUtils.isValidSupply(entity) && !entity.getDisplayName().getUnformattedText().contains("SUPPLY RECEIVED")) {
                double distance = mc.thePlayer.getDistanceToEntity(entity);
                float alpha = (float) Math.max(0.4, 1.0 - (distance / 100.0));

                // Create a green color with distance-based alpha
                RenderUtils.Color beamColor = new RenderUtils.Color(
                        0, 255, 0, (int)(alpha * 255)
                );

                // Render beacon beam at the entity's position
                RenderUtils.renderBeaconBeam(
                        new Vec3(entity.posX, entity.posY, entity.posZ),
                        beamColor,
                        true, // depth check
                        80.0f, // height
                        event.partialTicks
                );
            }
        }
    }

    private static boolean isEnabled() {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(MODULE_NAME)) {
                return module.enabled;
            }
        }
        return false;
    }
}