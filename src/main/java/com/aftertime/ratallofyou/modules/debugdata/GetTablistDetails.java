package com.aftertime.ratallofyou.modules.debugdata;

import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.TabUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class GetTablistDetails {
    /**
     * Print all tab list details and dungeon class info to console and in-game chat
     */
    public static void printTablistDetails() {
        if (!TabUtils.isInSkyblock() || !isEnabled()) {
            System.out.println("Not in SkyBlock. Tablist details not shown.");
            sendChatMessage("Not in SkyBlock. Tablist details not shown.");
            return;
        }

        // Print all tab list entries
        System.out.println("=== TAB LIST ENTRIES ===");
        sendChatMessage("=== TAB LIST ENTRIES ===");
        for (String entry : TabUtils.getTabList()) {
            System.out.println(entry);
            sendChatMessage(entry);
        }

        // Print dungeon class info
        System.out.println("=== DUNGEON CLASSES ===");
        sendChatMessage("=== DUNGEON CLASSES ===");
        for (TabUtils.PlayerClassInfo info : TabUtils.getDungeonClasses()) {
            String msg = info.getPlayerName() + " - " + info.getDungeonClass();
            System.out.println(msg);
            sendChatMessage(msg);
        }
    }

    private static void sendChatMessage(String msg) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(msg));
        }
    }
    public static boolean isEnabled() { return BooleanSettings.isEnabled("debugdata_gettablistdetails"); }
}
