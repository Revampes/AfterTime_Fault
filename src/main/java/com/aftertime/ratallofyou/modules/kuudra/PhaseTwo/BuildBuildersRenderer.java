package com.aftertime.ratallofyou.modules.kuudra.PhaseTwo;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BuildBuildersRenderer {
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!KuudraUtils.isPhase(2) || !isEnabled()) return;
        String text = KuudraUtils.getBuilders() + " builders";
        RenderUtils.renderFloatingText(text, -101.5, 79.125, -105.5, 10.0f, 0x00FFFF, false);
    }

    private boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_buildbuilders");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
