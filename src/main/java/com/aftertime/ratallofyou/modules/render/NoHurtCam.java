package com.aftertime.ratallofyou.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import com.aftertime.ratallofyou.config.ModConfig;

public class NoHurtCam {
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!ModConfig.enableNoHurtCam) return;

        if (mc.thePlayer != null) {
            // Reset both hurt timers at the beginning of render phase
            mc.thePlayer.hurtTime = 0;
            mc.thePlayer.maxHurtTime = 0;
        }
    }
}