package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

public class F7GhostBlocks {
    private static final String CONFIG_RESOURCE = "/com/aftertime/ratallofyou/Config/floorConfig.json";

    // Block position maps
    private static final Map<Integer, BlockPos[]> airs = new HashMap<Integer, BlockPos[]>();
    private static final Map<Integer, BlockPos[]> enderChests = new HashMap<Integer, BlockPos[]>();
    private static final Map<Integer, BlockPos[]> glass = new HashMap<Integer, BlockPos[]>();

    static {
        // Load positions from JSON resource on the classpath
        loadConfigFromJson();
    }

    public static void init() {
        DungeonUtils.init(new Runnable() {
            public void run() {
                cleanupGhostBlocks();
            }
        });
        MinecraftForge.EVENT_BUS.register(new F7GhostBlocks());
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !ModConfig.enableF7GhostBlocks || !(DungeonUtils.isInDungeonFloor() == 7)) {
            return;
        }

        //Null pointer check
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        if (DungeonUtils.isInDungeon() && DungeonUtils.isInDungeonFloor() == 7) {
            // Process in ascending phase order, based on loaded config
            Set<Integer> ecPhases = new TreeSet<Integer>(enderChests.keySet());
            Set<Integer> glassPhases = new TreeSet<Integer>(glass.keySet());
            for (Integer phaseNum : ecPhases) {
                processEnderChests(phaseNum);
            }
            for (Integer phaseNum : glassPhases) {
                processGlass(phaseNum);
            }
            Set<Integer> airPhases = new TreeSet<Integer>(airs.keySet());
            for (Integer phaseNum : airPhases) {
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

                    // Restore original blocks for all air phases
                    for (Integer phaseNum : new TreeSet<Integer>(airs.keySet())) {
                        restoreOriginalBlocks(phaseNum);
                    }
                    // Remove placed blocks for all phases present in either enderChests or glass
                    TreeSet<Integer> placedPhases = new TreeSet<Integer>();
                    placedPhases.addAll(enderChests.keySet());
                    placedPhases.addAll(glass.keySet());
                    for (Integer phaseNum : placedPhases) {
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

    // --- JSON loading helpers ---
    private static void loadConfigFromJson() {
        try {
            InputStream stream = F7GhostBlocks.class.getResourceAsStream(CONFIG_RESOURCE);
            if (stream == null) {
                return; // Resource not found, leave maps empty
            }
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            JsonObject root = new JsonParser().parse(reader).getAsJsonObject();
            reader.close();

            parseSection(root, "airs", airs);
            parseSection(root, "enderChests", enderChests);
            parseSection(root, "glass", glass);
        } catch (Exception ignored) {
            // Silent error handling
        }
    }

    private static void parseSection(JsonObject root, String key, Map<Integer, BlockPos[]> target) {
        try {
            if (root == null || !root.has(key)) return;
            JsonObject section = root.getAsJsonObject(key);
            if (section == null) return;
            for (Map.Entry<String, JsonElement> entry : section.entrySet()) {
                try {
                    int phase = Integer.parseInt(entry.getKey());
                    JsonArray arr = entry.getValue().getAsJsonArray();
                    BlockPos[] positions = new BlockPos[arr.size()];
                    for (int i = 0; i < arr.size(); i++) {
                        JsonObject obj = arr.get(i).getAsJsonObject();
                        int x = obj.get("x").getAsInt();
                        int y = obj.get("y").getAsInt();
                        int z = obj.get("z").getAsInt();
                        positions[i] = new BlockPos(x, y, z);
                    }
                    target.put(phase, positions);
                } catch (Exception ignored) {
                    // skip malformed entry
                }
            }
        } catch (Exception ignored) {
            // Silent error handling
        }
    }
}

