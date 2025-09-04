package com.aftertime.ratallofyou.modules.SkyBlock;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.DungeonUtils;

public class NoticeTitle {
    private String regrex;
    private String customShownTitle;

    public void onChat(ClientChatReceivedEvent event) {
        if (event == null || event.message == null || BooleanSettings.isEnabled("NoticeTitle")) return;
        String message = event.message.getUnformattedText();

        if (message.contains(regrex)) {

        }
    }
}
