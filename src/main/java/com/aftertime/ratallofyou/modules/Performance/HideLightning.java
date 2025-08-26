package com.aftertime.ratallofyou.modules.Performance;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HideLightning {
    private static boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("performance_hidelightning");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!isEnabled()) return;
        if (event == null || event.entity == null || event.world == null) return;
        if (!event.world.isRemote) return; // client-side only

        if (event.entity instanceof EntityLightningBolt) {
            event.setCanceled(true);
        }
    }
}
