package com.aftertime.ratallofyou.modules.kuudra;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import static com.aftertime.ratallofyou.Main.mc;

public class FreshMesage {
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();
        if (!isModuleEnabled() || msg == null) return;
        if (msg.contains("Your Fresh Tools Perk bonus doubles your building speed for the next 10 seconds!") &&
                KuudraUtils.isPhase(2)) {
            mc.thePlayer.sendChatMessage("/pc FRESH!");
        }
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra-freshmessage");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
