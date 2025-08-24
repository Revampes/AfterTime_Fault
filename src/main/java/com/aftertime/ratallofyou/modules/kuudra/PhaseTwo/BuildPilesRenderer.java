package com.aftertime.ratallofyou.modules.kuudra.PhaseTwo;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BuildPilesRenderer {
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!KuudraUtils.isPhase(2) || !isModuleEnabled()) return;
        for (KuudraUtils.StandInfo s : KuudraUtils.getBuildPilesStands()) {
            RenderUtils.renderBeaconBeam(new Vec3(s.x, s.y, s.z), new RenderUtils.Color(255, 0, 0, (int)(0.8f * 255)), true, 100f, event.partialTicks);
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_buildpiles");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}

