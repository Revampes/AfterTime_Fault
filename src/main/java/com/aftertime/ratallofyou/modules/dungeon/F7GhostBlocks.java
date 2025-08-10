//package com.aftertime.ratallofyou.modules.dungeon;
//
//import com.aftertime.ratallofyou.Main;
//import com.aftertime.ratallofyou.config.ModConfig;
//import com.aftertime.ratallofyou.modules.utils.DungeonUtils;
//import net.minecraft.client.Minecraft;
//import net.minecraft.init.Blocks;
//import net.minecraft.util.BlockPos;
//import net.minecraft.util.ChatComponentText;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.gameevent.TickEvent;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class F7GhostBlocks {
//    private static boolean debugEnabled = true;
//
//    private boolean isDebugEnabled() {
//        return ModConfig.ghostBlocksDebugEnabled;
//    }
//
//    private static final Map<Integer, BlockPos[]> enderChests = new HashMap<Integer, BlockPos[]>() {{
//        put(1, new BlockPos[]{
//                new BlockPos(69, 221, 37),
//                new BlockPos(69, 221, 36),
//        });
//        put(2, new BlockPos[]{
//                new BlockPos(101, 169, 46),
//                new BlockPos(100, 169, 46),
//                new BlockPos(99, 169, 46)
//        });
//        put(3, new BlockPos[]{
//                new BlockPos(56, 114, 111)
//        });
//    }};
//
//    private static final Map<Integer, BlockPos[]> glass = new HashMap<Integer, BlockPos[]>() {{
//        put(1, new BlockPos[]{
//                new BlockPos(68, 221, 38),
//                new BlockPos(69, 221, 38)
//        });
//        put(2, new BlockPos[]{
//                new BlockPos(102, 169, 47)
//        });
//        put(3, new BlockPos[]{
//                new BlockPos(55, 114, 110),
//                new BlockPos(55, 114, 111)
//        });
//    }};
//
//    private static final Map<Integer, BlockPos[]> blocks = new HashMap<Integer, BlockPos[]>() {{
//        put(1, new BlockPos[]{
//                new BlockPos(88, 220, 61),
//                new BlockPos(88, 219, 61),
//                new BlockPos(88, 218, 61),
//                new BlockPos(88, 217, 61),
//                new BlockPos(88, 216, 61),
//                new BlockPos(88, 215, 61),
//                new BlockPos(88, 214, 61),
//                new BlockPos(88, 213, 61),
//                new BlockPos(88, 212, 61),
//                new BlockPos(88, 211, 61),
//                new BlockPos(88, 210, 61),
//                new BlockPos(69, 220, 37),
//                new BlockPos(68, 220, 37)
//        });
//        put(2, new BlockPos[]{
//                // ... (same as original)
//        });
//        put(3, new BlockPos[]{
//                // ... (same as original)
//        });
//        put(4, new BlockPos[]{
//                // ... (same as original)
//        });
//    }};
//
//    @SubscribeEvent
//    public void onTick(TickEvent.ClientTickEvent event) {
//        if (event.phase != TickEvent.Phase.START || !ModConfig.preGhostBlockEnabled) return;
//
//        if (!DungeonUtils.isFloor7() || !DungeonUtils.inBossRoom()) {
//            if (debugEnabled) sendDebugMessage("Not in F7 boss room");
//            return;
//        }
//
//        DungeonUtils.M7Phases phase = DungeonUtils.getF7Phase();
//        if (phase == DungeonUtils.M7Phases.Unknown) {
//            if (debugEnabled) sendDebugMessage("Unknown phase detected");
//            return;
//        }
//
//        int phaseNum = phase.ordinal();
//        if (debugEnabled) sendDebugMessage("Current phase: " + phase + " (ordinal: " + phaseNum + ")");
//
//        processBlocks(phaseNum);
//        processEnderChests(phaseNum);
//        processGlass(phaseNum);
//    }
//
//    private void processBlocks(int phaseNum) {
//        BlockPos[] phaseBlocks = blocks.get(phaseNum);
//        if (phaseBlocks != null) {
//            for (BlockPos pos : phaseBlocks) {
//                if (debugEnabled) sendDebugMessage("Removing block at " + pos);
//                Minecraft.getMinecraft().theWorld.setBlockToAir(pos);
//            }
//        } else if (debugEnabled) {
//            sendDebugMessage("No blocks defined for phase " + phaseNum);
//        }
//    }
//
//    private void processEnderChests(int phaseNum) {
//        BlockPos[] phaseEnderChests = enderChests.get(phaseNum);
//        if (phaseEnderChests != null) {
//            for (BlockPos pos : phaseEnderChests) {
//                if (debugEnabled) sendDebugMessage("Placing ender chest at " + pos);
//                Minecraft.getMinecraft().theWorld.setBlockState(pos, Blocks.ender_chest.getDefaultState());
//            }
//        } else if (debugEnabled) {
//            sendDebugMessage("No ender chests defined for phase " + phaseNum);
//        }
//    }
//
//    private void processGlass(int phaseNum) {
//        BlockPos[] phaseGlass = glass.get(phaseNum);
//        if (phaseGlass != null) {
//            for (BlockPos pos : phaseGlass) {
//                if (debugEnabled) sendDebugMessage("Placing glass at " + pos);
//                Minecraft.getMinecraft().theWorld.setBlockState(pos, Blocks.stained_glass.getDefaultState());
//            }
//        } else if (debugEnabled) {
//            sendDebugMessage("No glass defined for phase " + phaseNum);
//        }
//    }
//
//    private void sendDebugMessage(String message) {
//        // Check if Minecraft instance, theWorld, and thePlayer exist
//        if (Minecraft.getMinecraft() != null &&
//                Minecraft.getMinecraft().theWorld != null &&
//                Minecraft.getMinecraft().thePlayer != null) {
//
//            Minecraft.getMinecraft().thePlayer.addChatMessage(
//                    new ChatComponentText("§a[GhostBlocks] §r" + message)
//            );
//        } else {
//            // Fallback to logging if player isn't available
//            System.out.println("[GhostBlocks] " + message);
//        }
//    }
//}