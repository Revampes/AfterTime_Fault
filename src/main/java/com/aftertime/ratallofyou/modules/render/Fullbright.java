package com.aftertime.ratallofyou.modules.render;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Fullbright {
    private static float originalGamma;
    private boolean wasEnabled = false;

    public Fullbright() {
        originalGamma = Minecraft.getMinecraft().gameSettings.gammaSetting;
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        boolean currentlyEnabled = isModuleEnabled();
        Minecraft mc = Minecraft.getMinecraft();

        if (currentlyEnabled) {
            mc.gameSettings.gammaSetting = 1000.0F;
            if (!wasEnabled) {
                wasEnabled = true;
            }
        } else if (wasEnabled) {
            mc.gameSettings.gammaSetting = originalGamma;
            wasEnabled = false;
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("render_fullbright");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}