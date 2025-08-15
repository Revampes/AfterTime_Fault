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
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

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
    private Color highlightColor = new Color(0, 255, 255, 128); // Default cyan color with alpha

    public SecretClicks() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static class HighlightedBlock {
        final BlockPos blockPos;
        final Block block;
        boolean locked;
        final long expireTime;

        HighlightedBlock(BlockPos blockPos, Block block, long expireTime) {
            this.blockPos = blockPos;
            this.block = block;
            this.locked = false;
            this.expireTime = expireTime;
        }
    }

    private void highlightBlock(BlockPos pos, Block block) {
        if (pos == null || block == null) return;

        String blockStr = pos.toString();
        long expireTime = Minecraft.getMinecraft().theWorld.getTotalWorldTime() + 100; // 100 ticks = 5 seconds

        // Remove existing highlight if present
        highlights.remove(blockStr);
        highlights.put(blockStr, new HighlightedBlock(pos, block, expireTime));

        if (!registered) {
            registered = true;
        }
    }

    private boolean isValidSkull(BlockPos pos) {
        if (pos == null) return false;

        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return false;

        TileEntity tileEntity = world.getTileEntity(pos);
        if (!(tileEntity instanceof TileEntitySkull)) return false;

        TileEntitySkull skull = (TileEntitySkull) tileEntity;
        if (skull.getPlayerProfile() == null) return false;

        UUID skullID = skull.getPlayerProfile().getId();
        if (skullID == null) return false;

        String skullIdStr = skullID.toString();
        for (String validId : VALID_SKULL_IDS) {
            if (validId.equals(skullIdStr)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isModuleEnabled() || !DungeonUtils.isInDungeon()) return;
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return;

        BlockPos pos = event.pos;
        if (pos == null) return;

        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;

        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
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
        if (!isModuleEnabled() || highlights.isEmpty() || event.isCanceled()) return;

        float r = highlightColor.getRed() / 255f;
        float g = highlightColor.getGreen() / 255f;
        float b = highlightColor.getBlue() / 255f;
        float a = highlightColor.getAlpha() / 255f;

        for (HighlightedBlock highlighted : highlights.values()) {
            if (highlighted.blockPos == null) continue;

            if (highlighted.locked) {
                renderBlockHighlight(highlighted.blockPos, 1, 0, 0, a);
            } else {
                renderBlockHighlight(highlighted.blockPos, r, g, b, a);
            }
        }
    }

    @SubscribeEvent
    public void onChat(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        if (!isModuleEnabled() || highlights.isEmpty()) return;

        if (event.message.getUnformattedText().equals("That chest is locked!")) {
            for (HighlightedBlock highlighted : highlights.values()) {
                if (highlighted.block != null &&
                        "minecraft:chest".equals(highlighted.block.getRegistryName())) {
                    highlighted.locked = true;
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

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
                if (hb.blockPos == null || hb.block == null) {
                    iterator.remove();
                    continue;
                }

                IBlockState state = world.getBlockState(hb.blockPos);
                if (state == null) {
                    iterator.remove();
                    continue;
                }

                Block currentBlock = state.getBlock();
                if (currentTime > hb.expireTime || currentBlock != hb.block) {
                    iterator.remove();
                }
            } catch (Exception e) {
                iterator.remove();
            }
        }

        if (highlights.isEmpty()) {
            registered = false;
        }
    }

    private void renderBlockHighlight(BlockPos pos, float r, float g, float b, float a) {
        if (pos == null) return;

        // Save current GL state
        boolean cullFace = GL11.glGetBoolean(GL11.GL_CULL_FACE);
        boolean depthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
        boolean texture2D = GL11.glGetBoolean(GL11.GL_TEXTURE_2D);
        boolean lighting = GL11.glGetBoolean(GL11.GL_LIGHTING);
        boolean blend = GL11.glGetBoolean(GL11.GL_BLEND);

        try {
            // Setup our rendering state
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_CULL_FACE); // Disable culling to render all faces
            GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
            GL11.glPolygonOffset(1.0f, -1.0f);

            // Draw solid box first (with lower alpha)
            HitBoxRenderer.renderBlockHitbox(pos, r, g, b, a * 0.5f, true, 2f, true);

            // Then draw outline
            HitBoxRenderer.renderBlockHitbox(pos, r, g, b, a, true, 2f, false);
        } finally {
            // Restore original GL state
            if (cullFace) GL11.glEnable(GL11.GL_CULL_FACE);
            else GL11.glDisable(GL11.GL_CULL_FACE);

            if (depthTest) GL11.glEnable(GL11.GL_DEPTH_TEST);
            else GL11.glDisable(GL11.GL_DEPTH_TEST);

            if (texture2D) GL11.glEnable(GL11.GL_TEXTURE_2D);
            else GL11.glDisable(GL11.GL_TEXTURE_2D);

            if (lighting) GL11.glEnable(GL11.GL_LIGHTING);
            else GL11.glDisable(GL11.GL_LIGHTING);

            if (blend) GL11.glEnable(GL11.GL_BLEND);
            else GL11.glDisable(GL11.GL_BLEND);

            GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
        }
    }

    private boolean isModuleEnabled() {
        if (ModConfig.MODULES == null) return false;

        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module != null && MODULE_NAME.equals(module.name)) {
                return module.enabled;
            }
        }
        return false;
    }
}