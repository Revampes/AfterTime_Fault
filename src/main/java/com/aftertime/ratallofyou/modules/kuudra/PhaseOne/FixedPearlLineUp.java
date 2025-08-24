package com.aftertime.ratallofyou.modules.kuudra.PhaseOne;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FixedPearlLineUp {
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        // Check both module enabled and phase 1
        if (!isModuleEnabled() || !KuudraUtils.isPhase(1)) {
            return;
        }

        // Triangle
        renderBox(-97, 157, -112, 1, 0, 0, 1, true);
        renderBox(-70.5, 79, -134.5, 1, 0, 0, 1, false);
        renderBox(-85.5, 78, -128.5, 1, 0, 0, 1, false);
        renderBox(-95.5, 161, -105.5, 1, 0, 1, 1, true);
        renderBox(-67.5, 77, -122.5, 1, 0, 1, 1, false);

        // X
        renderBox(-103, 160, -109, 1, 1, 1, 1, true);
        renderBox(-134.5, 77, -138.5, 1, 1, 1, 1, false);
        renderBox(-130.5, 79, -113.5, 1, 0.588f, 0.059f, 1, false);
        renderBox(-110, 155, -106, 1, 0.588f, 0.059f, 1, true);

        // Square
        renderBox(-43.5, 120, -149.5, 1, 1, 1, 1, true);
        renderBox(-45.5, 135, -138.5, 1, 1, 1, 1, true);
        renderBox(-35.5, 138, -124.5, 1, 1, 1, 1, true);
        renderBox(-26.5, 126, -111.5, 1, 1, 1, 1, true);
        renderBox(-140.5, 78, -90.5, 1, 1, 1, 1, false);

        // =
        renderBox(-106, 165, -101, 1, 1, 0, 1, true);
        renderBox(-65.5, 76, -87.5, 1, 1, 0, 1, false);

        // /
        renderBox(-105, 157, -98, 1, 0, 1, 1, true);
        renderBox(-112.5, 76.5, -68.5, 1, 0, 1, 1, false);
    }

    private static void renderBox(double x, double y, double z, double size,
                                  float r, float g, float b, boolean filled) {
        // For a 1-block size box centered at exact coordinates
        double halfSize = size / 2.0;
        double x0 = x - halfSize;
        double x1 = x + halfSize;
        // Don't offset Y coordinate - Minecraft blocks are aligned to whole numbers vertically
        double y0 = y;
        double y1 = y + size;  // Full height from the specified Y position
        double z0 = z - halfSize;
        double z1 = z + halfSize;

        RenderUtils.renderBoxFromCorners(x0, y0, z0, x1, y1, z1,
                r, g, b, 1.0f, true, 2.0f, filled);
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_fixedpearllineups");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}