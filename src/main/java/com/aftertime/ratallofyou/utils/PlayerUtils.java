package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.World;

import java.util.*;
import java.util.regex.Pattern;

public class PlayerUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Pattern MC_USERNAME = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

    /**
     * Strips rank and tags off player name.
     *
     * @param player Player name with rank and tags.
     * @return Base player IGN.
     */
    public static String getPlayerName(String player) {
        if (player == null) return "";
        String name = player;
        int nameIndex = name.indexOf(']');

        while (nameIndex != -1) {
            name = name.substring(nameIndex + 2);
            nameIndex = name.indexOf(']');
        }

        String[] parts = name.split(" ");
        return parts.length > 0 ? parts[0] : name;
    }

    /**
     * Strips the user of the rank.
     *
     * @param player Player's name with rank ex [MVP++] Da_Minty
     * @return Stripped rank ex Da_Minty
     */
    public static String stripRank(String player) {
        if (player == null) return "";
        String stripped = player;
        stripped = stripped.replace("[MVP++] ", " ");
        stripped = stripped.replace("[MVP+] ", " ");
        stripped = stripped.replace("[MVP] ", " ");
        stripped = stripped.replace("[VIP+] ", " ");
        stripped = stripped.replace("[VIP] ", " ");
        stripped = stripped.replace(" ", "");
        return stripped;
    }

    /**
     * Extracts and returns the guild name from a player's name string.
     *
     * @param player Player's name, possibly with guild tags and ranks.
     * @return Extracted guild name from the player's name.
     */
    public static String getGuildName(String player) {
        if (player == null) return "";
        String name = player;
        int rankIndex = name.indexOf("] ");

        if (rankIndex != -1) {
            name = name.substring(rankIndex + 2);
        }

        int guildStart = name.indexOf('[');
        if (guildStart == -1) return "";

        return name.substring(0, guildStart - 1);
    }

    /**
     * Returns True if entity is player otherwise False.
     *
     * @param entityName Name of the entity.
     * @return Whether or not player is human.
     */
    public static boolean isPlayer(String entityName) {
        if (entityName == null || entityName.isEmpty()) return false;

        // In Java/Forge, we need to use different approach than the JS version
        return MC_USERNAME.matcher(entityName).matches() &&
               mc.theWorld != null &&
               mc.theWorld.getPlayerEntityByName(entityName) != null;
    }

    /**
     * What class the player is playing on
     *
     * @return Returns the player's class
     */
    public static String getPlayerClass() {
        if (mc.thePlayer == null) return "?";

        List<String> tabInfo = TabUtils.getTabList();
        String playerName = mc.thePlayer.getName();

        for (String tabLine : tabInfo) {
            String cleanLine = net.minecraft.util.StringUtils.stripControlCodes(tabLine);
            if (cleanLine.contains(playerName)) {
                int startIdx = cleanLine.indexOf("(");
                if (startIdx != -1 && startIdx + 1 < cleanLine.length()) {
                    return cleanLine.substring(startIdx + 1, startIdx + 2).toUpperCase();
                }
            }
        }

        return "?";
    }

    /**
     * What class another player is playing on
     *
     * @param player Player clean username
     * @return Returns the player's class (H, M, T, A, B, or ?)
     */
    public static String getPlayerClassOther(String player) {
        if (player == null || player.isEmpty()) return "?";

        List<String> tabInfo = TabUtils.getTabList();
        String cleanPlayerName = getPlayerName(player);

        for (String tabLine : tabInfo) {
            String cleanLine = net.minecraft.util.StringUtils.stripControlCodes(tabLine);
            if (cleanLine.contains(cleanPlayerName)) {
                int startIdx = cleanLine.indexOf("(");
                if (startIdx != -1 && startIdx + 1 < cleanLine.length()) {
                    return cleanLine.substring(startIdx + 1, startIdx + 2).toUpperCase();
                }
            }
        }

        return "?";
    }

    /**
     * Get dungeon classes for all players in the tab list
     *
     * @return Map of player names to their dungeon class letters
     */
    public static Map<String, String> getDungeonClasses() {
        Map<String, String> playerClasses = new HashMap<>();
        List<String> tabLines = TabUtils.getTabList();

        // The dungeon tab format typically has "(Class)" at the end of each name
        for (String line : tabLines) {
            String cleanLine = net.minecraft.util.StringUtils.stripControlCodes(line);

            // Find the position of the class indicator
            int classStartIdx = cleanLine.indexOf("(");
            if (classStartIdx != -1 && classStartIdx + 1 < cleanLine.length()) {
                // Extract the class letter (first character in parentheses)
                String classLetter = cleanLine.substring(classStartIdx + 1, classStartIdx + 2).toUpperCase();

                // Make sure it's a valid dungeon class
                if ("HMTAB".contains(classLetter)) {
                    // Extract player name - this is approximate, would need refinement based on actual format
                    String playerName = cleanLine.substring(0, classStartIdx).trim();

                    // Strip any remaining formatting or tags
                    playerName = getPlayerName(playerName);

                    // Only add if it looks like a valid Minecraft username
                    if (MC_USERNAME.matcher(playerName).matches()) {
                        playerClasses.put(playerName, classLetter);
                    }
                }
            }
        }

        return playerClasses;
    }
}
