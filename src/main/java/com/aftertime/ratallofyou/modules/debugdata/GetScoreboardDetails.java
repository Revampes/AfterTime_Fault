package com.aftertime.ratallofyou.modules.debugdata;

import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.TabUtils;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import java.util.List;

public class GetScoreboardDetails {
    /**
     * Print all scoreboard lines and dungeon floor info to console and in-game chat
     */
    public static void printScoreboardDetails() {
        if (!TabUtils.isInSkyblock() || !isEnabled()) {
            System.out.println("Not in SkyBlock. Scoreboard details not shown.");
            sendChatMessage("Not in SkyBlock. Scoreboard details not shown.");
            return;
        }

        // Print all scoreboard lines
        List<String> lines = com.aftertime.ratallofyou.utils.Utils.getSidebarLines();
        System.out.println("=== SCOREBOARD LINES ===");
        sendChatMessage("=== SCOREBOARD LINES ===");
        if (lines != null) {
            for (String line : lines) {
                System.out.println(line);
                sendChatMessage(line);
            }
        }

        // Print dungeon floor info
        int floor = DungeonUtils.isInDungeonFloor();
        String floorMsg = "Dungeon Floor: " + (floor == 0 ? "Unknown" : floor);
        System.out.println(floorMsg);
        sendChatMessage(floorMsg);
    }

    private static void sendChatMessage(String msg) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(msg));
        }
    }
    public static boolean isEnabled() { return BooleanSettings.isEnabled("debugdata_getscoreboarddetails"); }
}
