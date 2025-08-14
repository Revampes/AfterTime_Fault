package com.aftertime.ratallofyou.modules.dungeon;

import com.aftertime.ratallofyou.UI.ModConfig;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.HitBoxRenderer;
import com.aftertime.ratallofyou.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class SecretClicks {
    private static final String MODULE_NAME = "Show Secret Clicks";
    private static final String[] VALID_BLOCKS = {
            "minecraft:chest",
            "minecraft:lever",
            "minecraft:skull",
            "minecraft:trapped_chest"
    };

    private static final String[] VALID_SKULL_IDS = {
            "e0f3e929-869e-3dca-9504-54c666ee6f23", // Wither Essence
            "fed95410-aba1-39df-9b95-1d4f361eb66e"  // Redstone Key
    };

    private final Map<String, HighlightedBlock> highlights = new HashMap<String, HighlightedBlock>();
    private boolean registered = false;
    private Color highlightColor = new Color(0, 255, 255); // Default cyan color

    public SecretClicks() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static class HighlightedBlock {
        BlockPos blockPos;
        Block block;
        boolean locked;
        long expireTime;

        HighlightedBlock(BlockPos blockPos, Block block, long expireTime) { // Update constructor
            this.blockPos = blockPos;
            this.block = block;
            this.locked = false;
            this.expireTime = expireTime;
        }
    }

    private void highlightBlock(BlockPos pos, Block block) {
        String blockStr = pos.toString();
        long expireTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime() + 100; // 100 ticks = 5 seconds
        highlights.put(blockStr, new HighlightedBlock(pos, block, expireTime)); // Pass expireTime

        if (!registered) {
            registered = true;
        }
        // Remove the scheduleBlockUpdate line since we'll handle timing differently
    }

    private boolean isValidSkull(BlockPos pos) {
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return false;

        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntitySkull)) return false;

        TileEntitySkull skull = (TileEntitySkull) tileEntity;
        if (skull.getPlayerProfile() == null) return false;

        UUID skullID = skull.getPlayerProfile().getId();
        if (skullID == null) return false;

        for (String validId : VALID_SKULL_IDS) {
            if (validId.equals(skullID.toString())) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onPacketSent(PlayerInteractEvent event) {
        if (!isModuleEnabled() || !DungeonUtils.isInDungeon()) return;
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        BlockPos pos = event.pos;
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;

        Block block = world.getBlockState(pos).getBlock();
        String blockName = block.getRegistryName();

        // Check if block is valid
        if (!Utils.isBlockValidForHighlight(world, pos)) return;

        // Special check for skulls
        if ("minecraft:skull".equals(blockName) && !isValidSkull(pos)) return;

        // Don't highlight if already highlighted
        if (highlights.containsKey(pos.toString())) return;

        highlightBlock(pos, block);

    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isModuleEnabled() || highlights.isEmpty()) return;

        float r = highlightColor.getRed() / 255f;
        float g = highlightColor.getGreen() / 255f;
        float b = highlightColor.getBlue() / 255f;

        for (HighlightedBlock highlighted : highlights.values()) {
            if (highlighted.locked) {
                renderBlockHighlight(highlighted.blockPos, 1, 0, 0);
            } else {
                renderBlockHighlight(highlighted.blockPos, r, g, b);
            }
        }
    }

    @SubscribeEvent
    public void onChat(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        if (!isModuleEnabled() || highlights.isEmpty()) return;

        if (event.message.getUnformattedText().equals("That chest is locked!")) {
            for (HighlightedBlock highlighted : highlights.values()) {
                if ("minecraft:chest".equals(highlighted.block.getRegistryName())) {
                    highlighted.locked = true;
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Get world reference safely
        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) {
            highlights.clear();
            registered = false;
            return;
        }

        long currentTime = world.getTotalWorldTime();
        Iterator<Map.Entry<String, HighlightedBlock>> iterator = highlights.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, HighlightedBlock> entry = iterator.next();
            HighlightedBlock hb = entry.getValue();

            try {
                // Check if block position is valid
                if (hb.blockPos == null) {
                    iterator.remove();
                    continue;
                }

                // Get block state safely
                IBlockState state = world.getBlockState(hb.blockPos);
                if (state == null) {
                    iterator.remove();
                    continue;
                }

                Block currentBlock = state.getBlock();

                // Remove if either:
                // 1. Time expired OR
                // 2. Block changed OR
                // 3. Current block is null
                if (currentTime > hb.expireTime ||
                        currentBlock == null ||
                        currentBlock != hb.block) {
                    iterator.remove();
                }
            } catch (Exception e) {
                // Safely remove problematic entries
                iterator.remove();
            }
        }

        // Unregister if no more highlights
        if (highlights.isEmpty()) {
            registered = false;
        }
    }

    private void renderBlockHighlight(BlockPos pos, float r, float g, float b) {
        // Draw solid box first (with lower alpha)
        HitBoxRenderer.renderBlockHitbox(pos, r, g, b, 0.3f, true, 2f, true);
        // Then draw outline
        HitBoxRenderer.renderBlockHitbox(pos, r, g, b, 1f, true, 2f, false);
    }

    private boolean isModuleEnabled() {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(MODULE_NAME)) {
                return module.enabled;
            }
        }
        return false;
    }
}