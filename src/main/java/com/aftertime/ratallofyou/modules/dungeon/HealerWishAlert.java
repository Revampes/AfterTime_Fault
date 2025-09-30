package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

public class HealerWishAlert {

    public void onChat(ClientChatReceivedEvent event) {
        if (!isModuleEnabled() || !DungeonUtils.isInDungeon() || !(DungeonUtils.isInDungeonFloor()==7)) return;

        String message = event.message.getUnformattedText();

        if (message.contains("⚠ Maxor is enraged! ⚠") ||
            message.contains("[BOSS] Goldor: You have done it, you destroyed the factory…")) {
            sendWishAlert();
        }
    }

    private void sendWishAlert() {
        DungeonUtils.sendTitle("Wish", "Press your QQQQQ", 5, 40, 5);
    }

    private boolean isModuleEnabled() {
        return BooleanSettings.isEnabled("dungeons_healerwishalert");
    }
}