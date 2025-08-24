package com.aftertime.ratallofyou.modules.kuudra.PhaseFourAndFive;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Renders an ESP box around the Kuudra boss (magma cube) while in Kuudra's Hollow.
 * Always enabled by default; no settings import.
 */
public class KuudraHitbox {

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!KuudraUtils.isInKuudraHollow() || !isModuleEnabled()) return;
        EntityMagmaCube kuudra = KuudraUtils.findKuudraBoss();
        if (kuudra == null) return;
        // Default color similar to a bright green outline
        RenderUtils.drawEntityBox(kuudra, 0.1f, 1.0f, 0.25f, 1.0f, 2.0f, event.partialTicks);
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_kuudrahitbox");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
