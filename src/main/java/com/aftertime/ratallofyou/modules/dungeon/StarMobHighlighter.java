package com.aftertime.ratallofyou.modules.render;

import com.aftertime.ratallofyou.UI.ModConfig;
import com.aftertime.ratallofyou.utils.BoxRenderer;
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
    private static final String MODULE_NAME = "Star Mob Highlighter";
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Pattern starredPattern = Pattern.compile(".*§6✯.*§c❤.*");

    // Colors for different mob types
    private static final float[] STAR_COLOR = {1f, 1f, 0f}; // Yellow
    private static final float[] SHADOW_ASSASSIN_COLOR = {0.67f, 0f, 1f}; // Purple

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new StarMobHighlighter());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Reset any state if needed
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isModuleEnabled() || mc.theWorld == null) return;

        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                String name = armorStand.getDisplayName().getUnformattedText();

                if (isStarredMob(name)) {
                    Entity mob = getMobEntity(armorStand);
                    if (mob != null) {
                        BoxRenderer.drawEntityEspBox(
                                mob.posX, mob.posY, mob.posZ,
                                mob.width, mob.height,
                                STAR_COLOR[0], STAR_COLOR[1], STAR_COLOR[2],
                                mob.isSneaking() ? -0.125f : 0f
                        );
                    }
                }
            }
            else if (entity instanceof EntityPlayer && "Shadow Assassin".equals(((EntityPlayer) entity).getDisplayName().getUnformattedText())) {
                EntityPlayer assassin = (EntityPlayer) entity;
                BoxRenderer.drawEntityEspBox(
                        assassin.posX, assassin.posY, assassin.posZ,
                        assassin.width, assassin.height,
                        SHADOW_ASSASSIN_COLOR[0], SHADOW_ASSASSIN_COLOR[1], SHADOW_ASSASSIN_COLOR[2],
                        assassin.isSneaking() ? -0.125f : 0f
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

    private boolean isModuleEnabled() {
        for (ModConfig.ModuleInfo module : ModConfig.MODULES) {
            if (module.name.equals(MODULE_NAME)) {
                return module.enabled;
            }
        }
        return false;
    }
}