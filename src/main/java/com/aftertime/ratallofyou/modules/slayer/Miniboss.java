package com.aftertime.ratallofyou.modules.slayer;

import com.aftertime.ratallofyou.UI.Settings.BooleanSettings;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Miniboss {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final String[] miniBossName = {
            "revenant sycophant", "revenant champion", "deformed revenant", "atoned champion", "atoned revenant",
            "tarantula vermin", "tarantula beast", "mutant tarantula",
            "pack enforcer", "sven follower", "sven alpha",
            "voidling devotee", "voidling radical", "voidcrazed maniac",
            "flare demon", "kindleheartdemon", "burningsoul demon"
    };

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new Miniboss());
    }

    // Color for miniboss box (red)
    private static final float[] MINIBOSS_COLOR = {1f, 0f, 0f};

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!BooleanSettings.isEnabled("slayer_miniboss") || mc.theWorld == null) return;

        for (Object entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityArmorStand) {
                EntityArmorStand armorStand = (EntityArmorStand) entity;
                String name = armorStand.getDisplayName().getUnformattedText().toLowerCase();
                if (isMiniBoss(name)) {
                    Entity mob = getMobEntity(armorStand);
                    if (mob != null) {
                        RenderUtils.drawEntityBox(
                                mob,
                                MINIBOSS_COLOR[0], MINIBOSS_COLOR[1], MINIBOSS_COLOR[2],
                                1.0f, // alpha
                                2.0f, // line width
                                event.partialTicks
                        );
                    }
                }
            }
        }
    }

    private boolean isMiniBoss(String name) {
        for (String miniBoss : miniBossName) {
            if (name.contains(miniBoss)) return true;
        }
        return false;
    }

    private Entity getMobEntity(EntityArmorStand armorStand) {
        for (Object entity : mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                armorStand,
                armorStand.getEntityBoundingBox().expand(0.5, 0.5, 0.5))) {
            if (entity instanceof Entity &&
                    !(entity instanceof EntityArmorStand) &&
                    !(entity instanceof EntityWither && ((Entity) entity).isInvisible()) &&
                    entity != mc.thePlayer) {
                return (Entity) entity;
            }
        }
        return armorStand;
    }
}
