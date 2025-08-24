package com.aftertime.ratallofyou.modules.kuudra.PhaseTwo;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FreshMessageHandler {
    private static final String FRESH_MSG = "Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!";

    @SubscribeEvent
    public void onChat(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();
        if (!KuudraUtils.isPhase(2) || !isModuleEnabled()) return;
        if (msg == null) return;
        if (msg.contains(FRESH_MSG)) {
            KuudraUtils.setFreshTime(System.currentTimeMillis());
            // broadcast party message
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                mc.thePlayer.sendChatMessage("/pc FRESH! (" + KuudraUtils.getBuild() + "%)");
            }
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra-freshmessage");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
