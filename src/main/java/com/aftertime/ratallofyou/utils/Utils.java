package com.aftertime.ratallofyou.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final Logger LOGGER = LogManager.getLogger("RatAllOfYou");

    // Pattern for Minecraft username validation
    private static final Pattern MC_USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{1,16}$");

    // Patterns for different class formats in tab list - improve matching precision
    private static final Pattern CLASS_PREFIX_PATTERN = Pattern.compile("^\\[(H|M|T|A|B)]\\s+([A-Za-z0-9_]{1,16})\\b");
    private static final Pattern NAME_CLASS_SUFFIX_PATTERN = Pattern.compile("^([A-Za-z0-9_]{1,16})\\s*[-•:]\\s*(Healer|Mage|Tank|Archer|Berserk(?:er)?)$", Pattern.CASE_INSENSITIVE);

    // Debug flag for showing tab list extraction info in chat
    private static boolean DEBUG_TAB_EXTRACTION = true;
    private static final Set<String> debuggedEntries = new HashSet<>();

    /**
     * Gets the current tab list (player list) entries as strings
     * @return List of tab list entries with formatting codes preserved
     */
    public static List<String> getTabListLines() {
        List<String> lines = new ArrayList<String>();
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null || mc.thePlayer == null || mc.getNetHandler() == null) return lines;

            Collection<NetworkPlayerInfo> playerInfoMap = mc.getNetHandler().getPlayerInfoMap();
            if (playerInfoMap == null) return lines;

            for (NetworkPlayerInfo info : playerInfoMap) {
                if (info == null) continue;

                String displayName = null;
                // Try to get formatted display name first
                IChatComponent component = info.getDisplayName();
                if (component != null) {
                    displayName = component.getFormattedText();
                }
                // Fall back to profile name if display name isn't available
                else if (info.getGameProfile() != null) {
                    displayName = info.getGameProfile().getName();
                }

                if (displayName != null && !displayName.isEmpty()) {
                    lines.add(displayName);
                }
            }

            // Debug: Show first 5 tab entries when tab list is read
            if (DEBUG_TAB_EXTRACTION && !lines.isEmpty()) {
                Minecraft mc2 = Minecraft.getMinecraft();
                if (mc2 != null && mc2.thePlayer != null && !lines.isEmpty()) {
                    String debugHash = String.valueOf(lines.size()) + ":" + lines.get(0);
                    if (!debuggedEntries.contains(debugHash)) {
                        debuggedEntries.add(debugHash);
                        mc2.thePlayer.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.DARK_AQUA + "[TabDebug] " +
                                EnumChatFormatting.AQUA + "Found " + lines.size() + " tab entries"));

                        for (int i = 0; i < Math.min(5, lines.size()); i++) {
                            String raw = lines.get(i);
                            String stripped = net.minecraft.util.StringUtils.stripControlCodes(raw);
                            mc2.thePlayer.addChatMessage(new ChatComponentText(
                                    EnumChatFormatting.DARK_AQUA + "[TabDebug] " +
                                    EnumChatFormatting.GRAY + "Entry " + i + ": " +
                                    EnumChatFormatting.WHITE + stripped));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Tab list read skipped: {}", e.toString());
        }
        return lines;
    }

    /**
     * Gets player names from the tab list that appear to be valid Minecraft usernames
     * @return List of player names
     */
    public static List<String> getPlayerNamesFromTab() {
        List<String> names = new ArrayList<String>();
        List<String> tabLines = getTabListLines();

        // Debug names found
        Set<String> debugNamesToShow = new HashSet<>();

        for (String line : tabLines) {
            // Strip formatting codes
            String plainText = net.minecraft.util.StringUtils.stripControlCodes(line);

            // Common formats:
            // 1. [Class] PlayerName
            // 2. PlayerName - Class
            // 3. Just PlayerName

            String candidateName = null;

            // Try format 1: [Class] PlayerName
            if (plainText.startsWith("[") && plainText.contains("]")) {
                int closeBracket = plainText.indexOf(']');
                if (closeBracket > 0 && closeBracket + 1 < plainText.length()) {
                    String afterBracket = plainText.substring(closeBracket + 1).trim();
                    int spaceIndex = afterBracket.indexOf(' ');
                    candidateName = (spaceIndex > 0) ? afterBracket.substring(0, spaceIndex) : afterBracket;

                    if (candidateName != null && !candidateName.isEmpty() &&
                            MC_USERNAME_PATTERN.matcher(candidateName).matches()) {
                        debugNamesToShow.add("Format1: [X] " + candidateName);
                    }
                }
            }
            // Try format 2: PlayerName - Class
            else if (plainText.contains("-") || plainText.contains(":")) {
                String[] parts = plainText.split("[-:]", 2);
                if (parts.length > 0) {
                    candidateName = parts[0].trim();
                    if (candidateName != null && !candidateName.isEmpty() &&
                            MC_USERNAME_PATTERN.matcher(candidateName).matches()) {
                        debugNamesToShow.add("Format2: " + candidateName + " - Class");
                    }
                }
            }
            // Try format 3: Just extract first word
            else {
                String[] parts = plainText.split("\\s+", 2);
                if (parts.length > 0) {
                    candidateName = parts[0].trim();
                    if (candidateName != null && !candidateName.isEmpty() &&
                            MC_USERNAME_PATTERN.matcher(candidateName).matches()) {
                        debugNamesToShow.add("Format3: " + candidateName);
                    }
                }
            }

            // Validate candidate is a Minecraft username
            if (candidateName != null && MC_USERNAME_PATTERN.matcher(candidateName).matches() && !names.contains(candidateName)) {
                names.add(candidateName);
            }
        }

        // Show debug info about player names
        if (DEBUG_TAB_EXTRACTION && !names.isEmpty()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                String debugHash = String.valueOf(names.size()) + ":" + String.join(",", names);
                if (!debuggedEntries.contains(debugHash)) {
                    debuggedEntries.add(debugHash);
                    mc.thePlayer.addChatMessage(new ChatComponentText(
                            EnumChatFormatting.LIGHT_PURPLE + "[NameDebug] " +
                            EnumChatFormatting.WHITE + "Found " + names.size() + " player names: " +
                            EnumChatFormatting.YELLOW + String.join(", ", names)));

                    // Show a few examples of how names were extracted
                    int shown = 0;
                    for (String debugInfo : debugNamesToShow) {
                        if (shown++ >= 3) break;  // Limit to 3 examples
                        mc.thePlayer.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.LIGHT_PURPLE + "[NameDebug] " +
                                EnumChatFormatting.GRAY + debugInfo));
                    }
                }
            }
        }

        return names;
    }

    /**
     * Extract dungeon class letters from Tab list, keyed by player name.
     * Supports formats like "[M] Player" or "Player - Mage"
     * @return Map of player names to class letters (H, M, T, A, B)
     */
    public static Map<String, String> getDungeonClassesFromTab() {
        Map<String, String> playerClasses = new HashMap<String, String>();
        List<String> tabLines = getTabListLines();

        // For debugging
        Map<String, String> debugEntries = new HashMap<>();
        int pattern1Count = 0;
        int pattern2Count = 0;
        int pattern3Count = 0;

        for (String line : tabLines) {
            // Strip formatting codes
            String plainText = net.minecraft.util.StringUtils.stripControlCodes(line).trim();

            // Try Class Prefix pattern: [M] PlayerName
            Matcher prefixMatcher = CLASS_PREFIX_PATTERN.matcher(plainText);
            if (prefixMatcher.find()) {
                String classLetter = prefixMatcher.group(1).toUpperCase(Locale.ENGLISH);
                String playerName = prefixMatcher.group(2);
                playerClasses.put(playerName, classLetter);
                debugEntries.put(playerName, "Pattern1: [" + classLetter + "] " + playerName);
                pattern1Count++;
                continue;
            }

            // Try Name - Class pattern: PlayerName - Mage
            Matcher suffixMatcher = NAME_CLASS_SUFFIX_PATTERN.matcher(plainText);
            if (suffixMatcher.find()) {
                String playerName = suffixMatcher.group(1);
                String classWord = suffixMatcher.group(2).toLowerCase(Locale.ENGLISH);
                String classLetter = null;

                // Map class word to letter - fixed to use startsWith() instead of contains()
                if (classWord.startsWith("heal")) classLetter = "H";
                else if (classWord.startsWith("mag")) classLetter = "M";
                else if (classWord.startsWith("tank")) classLetter = "T";
                else if (classWord.startsWith("arch")) classLetter = "A";
                else if (classWord.startsWith("ber")) classLetter = "B";

                if (classLetter != null) {
                    playerClasses.put(playerName, classLetter);
                    debugEntries.put(playerName, "Pattern2: " + playerName + " - " + classWord + " → " + classLetter);
                    pattern2Count++;
                }
                continue;
            }

            // Try general detection for any [X] format where X is a class letter
            if (plainText.length() > 3 && plainText.charAt(0) == '[' && plainText.charAt(2) == ']') {
                char classChar = Character.toUpperCase(plainText.charAt(1));
                if ("HMTAB".indexOf(classChar) != -1) {
                    // Only consider if character 3 is a space or other reasonable separator
                    if (plainText.charAt(3) == ' ' || plainText.charAt(3) == '|' || plainText.charAt(3) == ':') {
                        String afterBracket = plainText.substring(3).trim();
                        int spaceIndex = afterBracket.indexOf(' ');
                        String playerName = (spaceIndex > 0) ?
                                afterBracket.substring(0, spaceIndex) : afterBracket;

                        if (MC_USERNAME_PATTERN.matcher(playerName).matches()) {
                            playerClasses.put(playerName, String.valueOf(classChar));
                            debugEntries.put(playerName, "Pattern3: " + plainText + " → " + playerName + " [" + classChar + "]");
                            pattern3Count++;
                        }
                    }
                }
            }
        }

        // Show debug information for class detection
        if (DEBUG_TAB_EXTRACTION && !playerClasses.isEmpty()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                String debugHash = String.valueOf(playerClasses.size()) + ":" + String.join(",", playerClasses.keySet());
                if (!debuggedEntries.contains(debugHash)) {
                    debuggedEntries.add(debugHash);

                    // Summary of detected classes
                    StringBuilder summary = new StringBuilder();
                    summary.append(EnumChatFormatting.GREEN).append("[ClassDebug] ")
                           .append(EnumChatFormatting.WHITE).append("Found ").append(playerClasses.size())
                           .append(" classes (P1:").append(pattern1Count)
                           .append(", P2:").append(pattern2Count)
                           .append(", P3:").append(pattern3Count).append("): ");

                    // Add class mapping
                    for (Map.Entry<String, String> entry : playerClasses.entrySet()) {
                        String playerName = entry.getKey();
                        String classLetter = entry.getValue();
                        String color = "";

                        // Color code by class
                        switch (classLetter) {
                            case "H": color = EnumChatFormatting.LIGHT_PURPLE.toString(); break; // Healer: Pink
                            case "M": color = EnumChatFormatting.AQUA.toString(); break;         // Mage: Blue
                            case "T": color = EnumChatFormatting.GREEN.toString(); break;        // Tank: Green
                            case "A": color = EnumChatFormatting.YELLOW.toString(); break;       // Archer: Yellow
                            case "B": color = EnumChatFormatting.RED.toString(); break;          // Berserker: Red
                            default:  color = EnumChatFormatting.GRAY.toString(); break;         // Unknown: Gray
                        }

                        summary.append(color).append("[").append(classLetter).append("]")
                               .append(EnumChatFormatting.GRAY).append(playerName)
                               .append(EnumChatFormatting.WHITE).append(", ");
                    }

                    mc.thePlayer.addChatMessage(new ChatComponentText(summary.toString()));

                    // Show details of a few examples
                    int shown = 0;
                    for (Map.Entry<String, String> entry : debugEntries.entrySet()) {
                        if (shown++ >= 3) break;  // Limit to 3 examples
                        mc.thePlayer.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.GREEN + "[ClassDebug] " +
                                EnumChatFormatting.GRAY + entry.getValue()));
                    }

                    // Show example of raw tab entry if available
                    if (!tabLines.isEmpty()) {
                        String rawExample = tabLines.get(0);
                        mc.thePlayer.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.GREEN + "[ClassDebug] " +
                                EnumChatFormatting.DARK_GRAY + "Raw entry example: " +
                                EnumChatFormatting.WHITE + rawExample));
                    }
                }
            }
        }

        return playerClasses;
    }

    /**
     * Toggle debug mode for tab extraction
     */
    public static void toggleTabDebug() {
        DEBUG_TAB_EXTRACTION = !DEBUG_TAB_EXTRACTION;
        debuggedEntries.clear(); // Clear cache to show messages again

        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.AQUA + "[Tab Debug] " +
                    (DEBUG_TAB_EXTRACTION ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled")));
        }
    }

    /**
     * Gets the current sidebar (scoreboard) lines as a list of strings
     * @return List of sidebar lines, or empty list if no scoreboard is visible
     */
    public static List<String> getSidebarLines() {
        List<String> lines = new ArrayList<String>();
        try {
            // Guard against menu screen or no world loaded
            Minecraft mc = Minecraft.getMinecraft();
            if (mc == null) return lines;
            World world = mc.theWorld;
            if (world == null) return lines;

            Scoreboard scoreboard = world.getScoreboard();
            if (scoreboard == null) return lines;

            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1); // 1 is sidebar slot
            if (objective == null) return lines;

            Collection<Score> scores = scoreboard.getSortedScores(objective);
            if (scores == null || scores.isEmpty()) return lines;

            for (Score score : scores) {
                if (score == null) continue;
                String playerName = score.getPlayerName();
                if (playerName == null) continue;
                ScorePlayerTeam team = scoreboard.getPlayersTeam(playerName);
                String line = ScorePlayerTeam.formatPlayerName(team, playerName);
                if (line != null) lines.add(line);
            }
        } catch (Exception e) {
            // Swallow occasional scoreboard issues quietly; return whatever we have
            LOGGER.debug("Sidebar read skipped: {}", e.toString());
        }
        return lines;
    }

    /**
     * Checks if the characters of the search string appear in order (but not necessarily consecutively)
     * within the source string.
     *
     * @param source The string to search within
     * @param search The string to search for as a subsequence
     * @return true if search is a subsequence of source, false otherwise
     */
    public static boolean containedByCharSequence(String source, String search) {
        if (source == null || search == null) {
            return false;
        }

        int searchIndex = 0;
        int sourceLength = source.length();
        int searchLength = search.length();

        // Empty string is always contained
        if (searchLength == 0) {
            return true;
        }

        for (int i = 0; i < sourceLength && searchIndex < searchLength; i++) {
            if (source.charAt(i) == search.charAt(searchIndex)) {
                searchIndex++;
            }
        }

        return searchIndex == searchLength;
    }

    public static double[] getBlockBoundingBox(World world, BlockPos pos) {
        try {
            if (world == null || pos == null) {
                return new double[]{0, 0, 0, 1, 1, 1};
            }

            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();

            // Get the collision bounding box
            AxisAlignedBB aabb = block.getCollisionBoundingBox(world, pos, state);

            if (aabb == null) {
                // Default full block if no specific bounding box
                return new double[]{
                        pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
                };
            }

            return new double[]{
                    aabb.minX, aabb.minY, aabb.minZ,
                    aabb.maxX, aabb.maxY, aabb.maxZ
            };
        } catch (Exception e) {
            LOGGER.error("Error getting block bounds at {}: {}", pos, e.toString());
            return new double[]{0, 0, 0, 1, 1, 1};
        }
    }

    public static boolean isBlockValidForHighlight(World world, BlockPos pos) {
        try {
            if (world == null || pos == null) return false;

            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            String blockName = block.getRegistryName();

            // List of blocks we want to highlight
            String[] validBlocks = {
                    "minecraft:chest",
                    "minecraft:lever",
                    "minecraft:skull",
                    "minecraft:trapped_chest",
                    "minecraft:stone_button"
            };

            return Arrays.asList(validBlocks).contains(blockName);
        } catch (Exception e) {
            LOGGER.error("Error checking block validity: {}", e.toString());
            return false;
        }
    }

    /**
     * Cleans a scoreboard line by removing Minecraft formatting codes and non-printable characters.
     * @param scoreboard The raw scoreboard line
     * @return The cleaned string
     */
    public static String cleanSB(String scoreboard) {
        char[] nvString = net.minecraft.util.StringUtils.stripControlCodes(scoreboard).toCharArray();
        StringBuilder cleaned = new StringBuilder();
        for (char c : nvString) {
            if ((int) c > 20 && (int) c < 127) {
                cleaned.append(c);
            }
        }
        return cleaned.toString();
    }
}
