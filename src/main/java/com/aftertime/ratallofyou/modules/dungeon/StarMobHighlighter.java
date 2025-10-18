package com.aftertime.ratallofyou.modules.dungeon;


import com.aftertime.ratallofyou.config.ModConfig;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.regex.Pattern;

public class StarMobHighlighter {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Pattern starredPattern = Pattern.compile(".*\u00a76\u272f.*\u00a7c\u2764.*");

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new StarMobHighlighter());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Reset any state if needed
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!ModConfig.enableStarMobHighlighter || mc.theWorld == null) return;

        // Get colors from ModConfig
        java.awt.Color starColor = new java.awt.Color(ModConfig.starMobHighlighterStarColor, true);
        java.awt.Color shadowColor = new java.awt.Color(ModConfig.starMobHighlighterShadowColor, true);

        float[] starRGB = {starColor.getRed() / 255f, starColor.getGreen() / 255f, starColor.getBlue() / 255f};
        float[] shadowRGB = {shadowColor.getRed() / 255f, shadowColor.getGreen() / 255f, shadowColor.getBlue() / 255f};

        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                String name = armorStand.getDisplayName().getUnformattedText();

                if (isStarredMob(name)) {
                    Entity mob = getMobEntity(armorStand);
                    if (mob != null) {
                        RenderUtils.drawEntityBox(
                                mob,
                                starRGB[0], starRGB[1], starRGB[2],
                                1.0f, // alpha
                                2.0f, // line width
                                event.partialTicks
                        );
                    }
                }
            }
            else if (entity instanceof EntityPlayer && "Shadow Assassin".equals(((EntityPlayer) entity).getDisplayName().getUnformattedText())) {
                EntityPlayer assassin = (EntityPlayer) entity;
                RenderUtils.drawEntityBox(
                        assassin,
                        shadowRGB[0], shadowRGB[1], shadowRGB[2],
                        1.0f, // alpha
                        2.0f, // line width
                        event.partialTicks
                );
            }
        }
    }

    private boolean isStarredMob(String name) {
        return starredPattern.matcher(name).matches();
    }

    private Entity getMobEntity(EntityArmorStand armorStand) {
        // Find the nearest non-armorstand entity below the armor stand
        for (Object entity : mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                armorStand,
                armorStand.getEntityBoundingBox().offset(0, -1, 0))) {

            if (entity instanceof Entity &&
                    !(entity instanceof EntityArmorStand) &&
                    !(entity instanceof EntityWither && ((Entity) entity).isInvisible()) &&
                    entity != mc.thePlayer) {
                return (Entity) entity;
            }
        }
        return null;
    }
}