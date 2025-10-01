package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindCorrectLivid {
    private final Minecraft mc = Minecraft.getMinecraft();

    private static final Map<Character, String> lividNames = new HashMap<Character, String>() {{
        put('2', "Frog Livid");
        put('5', "Purple Livid");
        put('7', "Doctor Livid");
        put('9', "Scream Livid");
        put('a', "Smile Livid");
        put('c', "Hockey Livid");
        put('d', "Crossed Livid");
        put('e', "Arcade Livid");
        put('f', "Vendetta Livid");
    }};

    private boolean foundLivid = false;
    private Entity livid = null;
    private Entity lividTag = null;
    private boolean inBoss = false;
    private Thread thread = null;

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isEnabled() || !DungeonUtils.isInDungeon()) return;
        if (!foundLivid || livid == null) {
            // Add debug to see why rendering isn't happening
            if (foundLivid && livid == null) {
                debug("foundLivid is true but livid entity is null!");
            }
            return;
        }

        debug("Rendering ESP box for livid at: " + livid.posX + ", " + livid.posY + ", " + livid.posZ);

        // Draw a highlight box around the correct livid
        RenderUtils.drawEntityBox(
            livid,
            0.0f, 1.0f, 0.0f, // Green color (RGB)
            1.0f, // Alpha
            3.0f, // Line width
            event.partialTicks
        );

        // Also render floating text above the livid
        RenderUtils.renderFloatingText(
            "§a§lCORRECT LIVID",
            livid.posX,
            livid.posY + livid.height + 0.5,
            livid.posZ,
            1.0f, // Scale
            0x00FF00, // Green color
            false // No depth test so it's always visible
        );
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!isEnabled() || !DungeonUtils.isInDungeon()) return;
        if (inBoss) return;

        String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (message.equals("[BOSS] Livid: I respect you for making it to here, but I'll be your undoing.")) {
            inBoss = true;
            debug("Boss fight started - beginning livid detection");
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!isEnabled() || !DungeonUtils.isInDungeon()) return;

        if (!foundLivid && inBoss) {
            if (mc.theWorld == null) return;

            // Find all livid entities
            List<Entity> loadedLivids = mc.theWorld.loadedEntityList;
            int lividCount = 0;
            int totalEntitiesChecked = 0;
            int entitiesWithLividInName = 0;

            debug("Scanning " + loadedLivids.size() + " total entities for livids...");

            for (Entity entity : loadedLivids) {
                totalEntitiesChecked++;
                String name = entity.getName();

                if (name != null && name.contains("Livid")) {
                    entitiesWithLividInName++;
                    debug("Found entity with 'Livid' in name: '" + name + "' (length: " + name.length() + ")");

                    if (name.length() > 5) {
                        debug("Checking pattern: name[1]='" + name.charAt(1) + "' vs name[5]='" + name.charAt(5) + "'");
                        // Check if name[1] == name[5] (pattern matching from Kotlin code)
                        if (name.charAt(1) == name.charAt(5)) {
                            lividCount++;
                            debug("✓ Entity matches pattern! Count now: " + lividCount);
                            if (lividTag == null) {
                                lividTag = entity;
                                debug("Set lividTag to: '" + name + "'");
                            }
                        } else {
                            debug("✗ Entity does NOT match pattern");
                        }
                    } else {
                        debug("✗ Name too short (length " + name.length() + " <= 5)");
                    }
                }
            }

            debug("=== SCAN RESULTS ===");
            debug("Total entities: " + totalEntitiesChecked);
            debug("Entities with 'Livid' in name: " + entitiesWithLividInName);
            debug("Entities matching pattern: " + lividCount);
            debug("Need >8 to proceed, currently have: " + lividCount);

            if (lividCount > 8) {
                debug("✓ Found enough livids! Proceeding with detection...");
                if (lividTag != null) {
                    // Get the character from the livid tag name
                    char lividChar = lividTag.getName().charAt(5);
                    debug("Extracted character from lividTag: '" + lividChar + "'");
                    debug("Looking for livid type: " + lividNames.get(lividChar));

                    livid = findClosestLivid(lividChar);

                    if (livid != null) {
                        foundLivid = true;
                        debug("✓ SUCCESS! Found livid from tag: " + lividNames.get(lividChar) + " at " + livid.posX + "," + livid.posY + "," + livid.posZ);
                    } else {
                        debug("✗ FAILED! Could not find closest livid for character: '" + lividChar + "'");
                        // Debug what entities we're looking for
                        String targetName = lividNames.get(lividChar);
                        debug("Was looking for entity with exact name: '" + targetName + "'");
                        int playerEntities = 0;
                        for (Entity entity : mc.theWorld.loadedEntityList) {
                            if (entity instanceof EntityOtherPlayerMP) {
                                playerEntities++;
                                String entityName = entity.getName();
                                debug("Player entity found: '" + entityName + "'");
                            }
                        }
                        debug("Total EntityOtherPlayerMP entities: " + playerEntities);
                    }
                }

                // Start thread to check block color after delay
                if ((thread == null || !thread.isAlive()) && mc.theWorld != null) {
                    debug("Starting block color check thread...");
                    thread = new Thread(() -> {
                        try {
                            Thread.sleep(1500); // Wait 1.5 seconds
                            debug("Block color check thread waking up...");

                            if (mc.theWorld != null) {
                                BlockPos blockPos = new BlockPos(5, 109, 42);
                                IBlockState state = mc.theWorld.getBlockState(blockPos);
                                debug("Block at " + blockPos + " is: " + state.getBlock().getClass().getSimpleName());

                                if (state.getBlock() instanceof BlockStainedGlass) {
                                    EnumDyeColor color = state.getValue(BlockStainedGlass.COLOR);
                                    char correctChar = getCharFromColor(color);
                                    debug("Block color: " + color.getName() + " -> mapped to char: '" + correctChar + "'");

                                    if (correctChar != 0) {
                                        debug("Searching for livid type: " + lividNames.get(correctChar));
                                        Entity correctLivid = findClosestLivid(correctChar);
                                        if (correctLivid != null) {
                                            livid = correctLivid;
                                            foundLivid = true;
                                            debug("✓ SUCCESS! Found correct livid from block color: " + lividNames.get(correctChar));
                                        } else {
                                            debug("✗ FAILED! Could not find livid for block color character: '" + correctChar + "'");
                                        }
                                    } else {
                                        debug("✗ ERROR: Unknown color detected: " + color.getName());
                                    }
                                } else {
                                    debug("✗ Block is not stained glass: " + state.getBlock().getClass().getSimpleName());
                                }
                            } else {
                                debug("✗ World is null in thread");
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            debug("Block color check thread interrupted");
                        }
                    }, "Livid Check");
                    thread.start();
                }
            } else {
                debug("✗ Not enough livids found (" + lividCount + " <= 8), continuing to scan...");
            }
        }
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        // Reset state when changing worlds
        foundLivid = false;
        livid = null;
        lividTag = null;
        inBoss = false;
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
        debug("World changed - reset livid detection");
    }

    private Entity findClosestLivid(char chatFormatting) {
        if (mc.theWorld == null || lividTag == null) return null;

        String targetName = lividNames.get(chatFormatting);
        if (targetName == null) return null;

        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityOtherPlayerMP) {
                String entityName = entity.getName();
                if (entityName != null && entityName.equals(targetName)) {
                    double distance = lividTag.getDistanceSqToEntity(entity);
                    if (distance < closestDistance) {
                        closest = entity;
                        closestDistance = distance;
                    }
                }
            }
        }

        return closest;
    }

    private char getCharFromColor(EnumDyeColor color) {
        switch (color) {
            case GREEN: return '2';      // Frog Livid
            case PURPLE: return '5';     // Purple Livid
            case GRAY: return '7';       // Doctor Livid
            case BLUE: return '9';       // Scream Livid
            case LIME: return 'a';       // Smile Livid
            case RED: return 'c';        // Hockey Livid
            case MAGENTA: return 'd';    // Crossed Livid
            case YELLOW: return 'e';     // Arcade Livid
            case WHITE: return 'f';      // Vendetta Livid
            default: return 0;           // Unknown color
        }
    }

    private boolean isEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("dungeons_findcorrectlivid");
        return cfg != null && (Boolean) cfg.Data;
    }

    private void debug(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.DARK_AQUA + "[FindCorrectLivid] " +
                EnumChatFormatting.GRAY + message
            ));
        }
    }
}
