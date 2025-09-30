package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TabUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    /**
     * Get all tab list entries as strings
     * @return List of player tab entries with formatting codes
     */
    public static List<String> getTabList() {
        List<String> tabList = new ArrayList<>();

        if (mc.thePlayer == null || mc.theWorld == null || mc.getNetHandler() == null) {
            return tabList;
        }

        NetHandlerPlayClient netHandler = mc.getNetHandler();
        Collection<NetworkPlayerInfo> playerInfoMap = netHandler.getPlayerInfoMap();

        for (NetworkPlayerInfo info : playerInfoMap) {
            if (info != null && info.getDisplayName() != null) {
                tabList.add(info.getDisplayName().getFormattedText());
            } else if (info != null && info.getGameProfile() != null) {
                tabList.add(info.getGameProfile().getName());
            }
        }

        return tabList;
    }

    /**
     * Get player names from tab list (useful for party member detection)
     * @return List of actual player names without formatting
     */
    public static List<String> getPlayerNames() {
        if (mc.thePlayer == null || mc.theWorld == null || mc.getNetHandler() == null) {
            return new ArrayList<>();
        }

        NetHandlerPlayClient netHandler = mc.getNetHandler();
        return netHandler.getPlayerInfoMap().stream()
                .filter(info -> info != null && info.getGameProfile() != null)
                .map(info -> info.getGameProfile().getName())
                .collect(Collectors.toList());
    }

    /**
     * Extract player IGNs and dungeon classes from tab list
     * @return Map of player names to their dungeon classes
     */
    public static List<PlayerClassInfo> getDungeonClasses() {
        List<PlayerClassInfo> result = new ArrayList<>();
        List<String> tabList = getTabList();

        // Debug tab contents for development
        System.out.println("=== TAB LIST DEBUG ===");
        for (String line : tabList) {
            System.out.println(line);
        }

        // Look for party members section in tab
        boolean partyMemberSection = false;

        for (String line : tabList) {
            // Look for Party Members: header
            if (line.contains("Party Members")) {
                partyMemberSection = true;
                continue;
            }

            if (partyMemberSection) {
                // Empty line marks end of party member section
                if (line.trim().isEmpty()) {
                    break;
                }

                // Extract player and class data
                // Format is typically: [CLASS] PLAYER_NAME
                String cleanLine = line.replaceAll("§.", "").trim();

                // Detect dungeon class from color codes
                String dungeonClass = "Unknown";
                if (line.contains("§a")) dungeonClass = "Tank";
                else if (line.contains("§6")) dungeonClass = "Archer";
                else if (line.contains("§d")) dungeonClass = "Healer";
                else if (line.contains("§c")) dungeonClass = "Berserker";
                else if (line.contains("§b")) dungeonClass = "Mage";

                // Extract player name (comes after class indicator)
                String playerName = cleanLine;
                if (cleanLine.contains(" ")) {
                    playerName = cleanLine.substring(cleanLine.indexOf(" ")).trim();
                    // Remove party rank indicators like [M]
                    if (playerName.contains("[") && playerName.contains("]")) {
                        playerName = playerName.replaceAll("\\[.*?\\]", "").trim();
                    }
                }

                if (!playerName.isEmpty()) {
                    result.add(new PlayerClassInfo(playerName, dungeonClass));
                }
            }
        }

        return result;
    }

    /**
     * Check if player is in SkyBlock by examining scoreboard title
     */
    public static boolean isInSkyblock() {
        if (mc.theWorld == null || mc.thePlayer == null) return false;

        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return false;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return false;

        String title = objective.getDisplayName();
        return title.contains("SKYBLOCK");
    }

    /**
     * Get all lines from the scoreboard
     */
    public static List<String> getScoreboardLines() {
        List<String> lines = new ArrayList<>();

        if (mc.theWorld == null) return lines;

        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) return lines;

        ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) return lines;

        Collection<String> scores = scoreboard.getObjectiveNames();
        for (String line : scores) {
            String formattedLine = scoreboard.getValueFromObjective(line, objective).getScorePoints() + ": " + line;
            lines.add(formattedLine);
        }

        return lines;
    }

    /**
     * Class to store player name and dungeon class info
     */
    public static class PlayerClassInfo {
        private final String playerName;
        private final String dungeonClass;

        public PlayerClassInfo(String playerName, String dungeonClass) {
            this.playerName = playerName;
            this.dungeonClass = dungeonClass;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getDungeonClass() {
            return dungeonClass;
        }
    }
}
