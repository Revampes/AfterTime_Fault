package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.Vec3;
import java.util.ArrayList;
import java.util.List;

public class KuudraUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static List<Vec3> findNearbySupplies(double maxDistance) {
        List<Vec3> supplies = new ArrayList<Vec3>();
        if (mc.theWorld == null || mc.thePlayer == null) return supplies;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (isValidSupply(entity)) {
                double distance = mc.thePlayer.getDistanceToEntity(entity);
                if (distance <= maxDistance) {
                    supplies.add(new Vec3(entity.posX, entity.posY, entity.posZ));
                }
            }
        }
        return supplies;
    }

    public static boolean isValidSupply(Entity entity) {
        if (!(entity instanceof EntityArmorStand)) return false;

        EntityArmorStand stand = (EntityArmorStand) entity;
        String name = stand.getDisplayName().getUnformattedText();

        if (name.contains("RECEIVED")) return false;

        // More robust name checking
        return name != null &&
                (name.contains("CLICK TO PICK UP") ||
                        name.contains("SUPPLIES") ||
                        name.contains("KUUDRA CRATE"));
    }

    public static boolean isInteractable(Entity entity) {
        return isValidSupply(entity) &&
                mc.thePlayer.getDistanceToEntity(entity) <= 4;
    }
}