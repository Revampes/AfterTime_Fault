package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.UI.ModConfig;
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

        boolean currentlyEnabled = isEnabled();
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

    private boolean isEnabled() {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals("FullBright")) {
                return module.enabled;
            }
        }
        return false;
    }
}