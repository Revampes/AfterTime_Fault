package com.aftertime.ratallofyou.Command;

import com.aftertime.ratallofyou.UI.config.ModSettingsGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GuiOpenScheduler {
    private boolean pending = true;

    public static void openConfigNextTick() {
        MinecraftForge.EVENT_BUS.register(new GuiOpenScheduler());
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!pending) return;
        pending = false;
        final Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            mc.displayGuiScreen(new ModSettingsGui());
        }
        MinecraftForge.EVENT_BUS.unregister(this);
    }
}

