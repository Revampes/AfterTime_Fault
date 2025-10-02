package com.aftertime.ratallofyou.modules.dungeon;


import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.DungeonUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import com.aftertime.ratallofyou.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class FindCorrectLivid {
    static BlockPos pos = new BlockPos(5, 108, 25);
    static Entity livid = null;
    static int tickAmount = 1;

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        livid = null;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!BooleanSettings.isEnabled("dungeons_findcorrectlivid")) return;
        tickAmount++;
        if (tickAmount % 10 == 0) {
            World world = Minecraft.getMinecraft().theWorld;
            if (world == null) return;
            int floor = DungeonUtils.isInDungeonFloor();
            if (floor == 5 /* F5 */ || floor == 15 /* M5, adjust if needed */) {
                List<String> scoreboard = Utils.getSidebarLines();
                if (scoreboard.size() == 0) return;
                String firstLine = Utils.cleanSB(scoreboard.get(scoreboard.size() - 1));
                System.out.println("[LividSolver] Cleaned scoreboard line: " + firstLine);
                if (firstLine.toLowerCase().contains("livid")) {
                    if (world.getBlockState(pos).getBlock() == Blocks.wool) {
                        int color = world.getBlockState(pos).getBlock().getDamageValue(world, pos);
                        String find = getTextFromValue(color);
                        System.out.println("[LividSolver] Wool color: " + color + ", find string: " + find);
                        List<Entity> entities = world.getLoadedEntityList();
                        for (Entity entity : entities) {
                            if (!(entity instanceof EntityArmorStand) || !entity.hasCustomName()) continue;
                            String name = entity.getCustomNameTag();
                            System.out.println("[LividSolver] Checking entity: " + name);
                            if (name.contains(find)) {
                                System.out.println("[LividSolver] Found correct Livid: " + name);
                                livid = entity;
                                return;
                            }
                        }
                        System.out.println("[LividSolver] No correct Livid found.");
                        livid = null;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRenderEntity(RenderLivingEvent.Pre<EntityLivingBase> event) {
        if (!BooleanSettings.isEnabled("dungeons_findcorrectlivid") || livid == null) return;

        Entity entity = event.entity;
        if (entity instanceof EntityArmorStand && entity.hasCustomName()) {
            String name = entity.getCustomNameTag();
            if (!entity.isEntityEqual(livid) && name.contains("Livid")) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (BooleanSettings.isEnabled("dungeons_findcorrectlivid") && livid != null) {
            // Draw ESP box around the correct Livid entity using its bounding box and interpolated position
            RenderUtils.drawEntityBox(livid, 1.0f, 0.0f, 0.0f, 1.0f, 2.0f, event.partialTicks);
            // Draw a larger, fixed-size box aligned with the entity's bounding box
            double[] interp = RenderUtils.getInterpolatedPosition(livid, event.partialTicks);
            double x = interp[0];
            double y = interp[1];
            double z = interp[2];
            double width = 0.6;
            double height = 2.0;
            AxisAlignedBB bigBox = new AxisAlignedBB(
                x - width/2, y, z - width/2,
                x + width/2, y + height, z + width/2
            );
            RenderUtils.drawEspBox(bigBox, 1.0f, 0.0f, 0.0f, 1.0f, 2.0f);
        }
    }

    static String getTextFromValue(int value) {
        String colour = "Failed";
        switch (value) {
            case 0:
                colour = EnumChatFormatting.WHITE.toString();
                break;
            case 2:
                colour = EnumChatFormatting.LIGHT_PURPLE.toString();
                break;
            case 4:
                colour = EnumChatFormatting.YELLOW.toString();
                break;
            case 5:
                colour = EnumChatFormatting.GREEN.toString();
                break;
            case 7:
                colour = EnumChatFormatting.GRAY.toString();
                break;
            case 10:
                colour = EnumChatFormatting.DARK_PURPLE.toString();
                break;
            case 11:
                colour = EnumChatFormatting.BLUE.toString();
                break;
            case 13:
                colour = EnumChatFormatting.DARK_GREEN.toString();
                break;
            case 14:
                colour = EnumChatFormatting.RED.toString();
                break;
        }
        return colour + EnumChatFormatting.BOLD + "Livid";
    }
}
