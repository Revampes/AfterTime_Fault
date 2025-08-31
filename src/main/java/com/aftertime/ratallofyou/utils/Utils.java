package com.aftertime.ratallofyou.utils;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Utils {
    private static final Logger LOGGER = LogManager.getLogger("RatAllOfYou");

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
}