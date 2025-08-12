package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.ModConfig;
import com.aftertime.ratallofyou.modules.utils.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;

public class F7GhostBlocks {
    private static final String MODULE_NAME = "Dungeon Sweat Mode (Use at your own risk)";
//    private static final String CONFIG_PATH = "src/main/java/com/aftertime/ratallofyou/Config/floorConfig.json";

    // Block position maps
    private static final Map<Integer, BlockPos[]> airs = new HashMap<Integer, BlockPos[]>();
    private static final Map<Integer, BlockPos[]> enderChests = new HashMap<Integer, BlockPos[]>();
    private static final Map<Integer, BlockPos[]> glass = new HashMap<Integer, BlockPos[]>();

    static {
        // Initialize block positions
        airs.put(1, new BlockPos[]{
                new BlockPos(88, 220, 61),
                new BlockPos(88, 219, 61),
                new BlockPos(88, 218, 61),
                new BlockPos(88, 217, 61),
                new BlockPos(88, 216, 61),
                new BlockPos(88, 215, 61),
                new BlockPos(88, 214, 61),
                new BlockPos(88, 213, 61),
                new BlockPos(88, 212, 61),
                new BlockPos(88, 211, 61),
                new BlockPos(88, 210, 61),
                new BlockPos(69, 220, 37),
                new BlockPos(68, 220, 37)
        });
        airs.put(2, new BlockPos[]{
        new BlockPos(88, 167, 41),
                new BlockPos(89, 167, 41),
                new BlockPos(91, 167, 41),
                new BlockPos(92, 167, 41),
                new BlockPos(93, 167, 41),
                new BlockPos(94, 167, 41),
                new BlockPos(95, 167, 41),
                new BlockPos(88, 166, 41),
                new BlockPos(89, 166, 41),
                new BlockPos(90, 166, 41),
                new BlockPos(91, 166, 41),
                new BlockPos(92, 166, 41),
                new BlockPos(93, 166, 41),
                new BlockPos(94, 166, 41),
                new BlockPos(95, 166, 41),
                new BlockPos(88, 165, 41),
                new BlockPos(89, 165, 41),
                new BlockPos(90, 165, 41),
                new BlockPos(91, 165, 41),
                new BlockPos(92, 165, 41),
                new BlockPos(93, 165, 41),
                new BlockPos(94, 165, 41),
                new BlockPos(95, 165, 41),
                new BlockPos(88, 167, 40),
                new BlockPos(89, 167, 40),
                new BlockPos(90, 167, 40),
                new BlockPos(91, 167, 40),
                new BlockPos(92, 167, 40),
                new BlockPos(93, 167, 40),
                new BlockPos(94, 167, 40),
                new BlockPos(95, 167, 40),
                new BlockPos(88, 166, 40),
                new BlockPos(89, 166, 40),
                new BlockPos(90, 166, 40),
                new BlockPos(91, 166, 40),
                new BlockPos(92, 166, 40),
                new BlockPos(93, 166, 40),
                new BlockPos(94, 166, 40),
                new BlockPos(95, 166, 40),
                new BlockPos(88, 165, 40),
                new BlockPos(89, 165, 40),
                new BlockPos(90, 165, 40),
                new BlockPos(91, 165, 40),
                new BlockPos(92, 165, 40),
                new BlockPos(93, 165, 40),
                new BlockPos(94, 165, 40),
                new BlockPos(95, 165, 40),
                new BlockPos(101, 168, 47),
                new BlockPos(101, 168, 46),
                new BlockPos(101, 167, 47),
                new BlockPos(101, 166, 47),
                new BlockPos(101, 167, 46),
                new BlockPos(101, 166, 46)
        });
        airs.put(3, new BlockPos[]{
                new BlockPos(51, 114, 52),
                new BlockPos(51, 114, 53),
                new BlockPos(51, 114, 54),
                new BlockPos(51, 114, 55),
                new BlockPos(51, 114, 56),
                new BlockPos(51, 114, 57),
                new BlockPos(51, 114, 58),
                new BlockPos(51, 115, 52),
                new BlockPos(51, 115, 53),
                new BlockPos(51, 115, 54),
                new BlockPos(51, 115, 55),
                new BlockPos(51, 115, 56),
                new BlockPos(51, 115, 57),
                new BlockPos(51, 115, 58),
                new BlockPos(56, 113, 111),
                new BlockPos(56, 112, 110),
                new BlockPos(56, 112, 110),
                new BlockPos(56, 111, 110)
        });
        airs.put(4, new BlockPos[]{
                new BlockPos(54, 64, 72),
                new BlockPos(54, 64, 73),
                new BlockPos(54, 63, 73),
                new BlockPos(54, 64, 74),
                new BlockPos(54, 63, 74)
        });

        enderChests.put(1, new BlockPos[]{
                new BlockPos(69, 221, 37),
                new BlockPos(69, 221, 36)
        });
        enderChests.put(2, new BlockPos[]{
                new BlockPos(101, 169, 46),
                new BlockPos(100, 169, 46),
                new BlockPos(99, 169, 46)
        });
        enderChests.put(3, new BlockPos[]{
                new BlockPos(56, 114, 111)
        });


        glass.put(1, new BlockPos[]{
                new BlockPos(68, 221, 38),
                new BlockPos(69, 221, 38)
        });
        glass.put(2, new BlockPos[]{
                new BlockPos(102, 169, 47)
        });
        glass.put(3, new BlockPos[]{
                new BlockPos(55, 114, 110),
                new BlockPos(55, 114, 111)
        });
    }

    public static void init() {
        DungeonUtils.init(new Runnable() {
            public void run() {
                cleanupGhostBlocks();
            }
        });
        MinecraftForge.EVENT_BUS.register(new F7GhostBlocks());
    }

    private boolean isModuleEnabled() {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(MODULE_NAME)) {
                return module.enabled;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !isModuleEnabled()) {
            return;
        }

        //Null pointer check
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        if (DungeonUtils.isInDungeon()) {
            for (int phaseNum = 1; phaseNum <= 3; phaseNum++) {
                processEnderChests(phaseNum);
                processGlass(phaseNum);
            }
            for (int phaseNum = 1; phaseNum <= 4; phaseNum++) {
                processBlocks(phaseNum);
            }
        }
    }

    private void processBlocks(int phaseNum) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null) return;

            BlockPos[] phaseBlocks = airs.get(phaseNum);
            if (phaseBlocks != null) {
                for (BlockPos pos : phaseBlocks) {
                    mc.theWorld.setBlockToAir(pos);
                }
            }
        } catch (Exception e) {
            // Silent error handling
        }
    }

    private void processEnderChests(int phaseNum) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null) return;

            BlockPos[] phaseEnderChests = enderChests.get(phaseNum);
            if (phaseEnderChests != null) {
                for (BlockPos pos : phaseEnderChests) {
                    mc.theWorld.setBlockState(pos, Blocks.ender_chest.getDefaultState());
                }
            }
        } catch (Exception e) {
            // Silent error handling
        }
    }

    private void processGlass(int phaseNum) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null) return;

            BlockPos[] phaseGlass = glass.get(phaseNum);
            if (phaseGlass != null) {
                for (BlockPos pos : phaseGlass) {
                    mc.theWorld.setBlockState(pos, Blocks.stained_glass.getDefaultState());
                }
            }
        } catch (Exception e) {
            // Silent error handling
        }
    }

    private static void cleanupGhostBlocks() {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            public void run() {
                try {
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc.theWorld == null) return;

                    for (int phaseNum = 1; phaseNum <= 3; phaseNum++) {
                        restoreOriginalBlocks(phaseNum);
                        removePlacedBlocks(phaseNum);
                    }
                } catch (Exception e) {
                    // Silent error handling
                }
            }
        });
    }

    private static void restoreOriginalBlocks(int phaseNum) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null) return;

            BlockPos[] positions = airs.get(phaseNum);
            if (positions != null) {
                for (BlockPos pos : positions) {
                    mc.theWorld.markBlockForUpdate(pos);
                }
            }
        } catch (Exception e) {
            // Silent error handling
        }
    }

    private static void removePlacedBlocks(int phaseNum) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null) return;

            BlockPos[] chests = enderChests.get(phaseNum);
            if (chests != null) {
                for (BlockPos pos : chests) {
                    mc.theWorld.setBlockToAir(pos);
                }
            }

            BlockPos[] glassPositions = glass.get(phaseNum);
            if (glassPositions != null) {
                for (BlockPos pos : glassPositions) {
                    mc.theWorld.setBlockToAir(pos);
                }
            }
        } catch (Exception e) {
            // Silent error handling
        }
    }
}