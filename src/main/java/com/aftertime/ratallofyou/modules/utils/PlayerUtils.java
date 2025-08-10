//package com.aftertime.ratallofyou.modules.utils;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.util.BlockPos;
//import net.minecraft.util.Vec3;
//
//public class PlayerUtils {
//    public static double getPosX() {
//        return Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.posX : 0.0;
//    }
//
//    public static double getPosY() {
//        return Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.posY : 0.0;
//    }
//
//    public static double getPosZ() {
//        return Minecraft.getMinecraft().thePlayer != null ? Minecraft.getMinecraft().thePlayer.posZ : 0.0;
//    }
//
//    public static String getPositionString() {
//        BlockPos blockPos = new BlockPos(getPosX(), getPosY(), getPosZ());
//        return String.format("x: %d, y: %d, z: %d", blockPos.getX(), blockPos.getY(), blockPos.getZ());
//    }
//
//    public static Vec3 getPositionVector() {
//        return new Vec3(getPosX(), getPosY(), getPosZ());
//    }
//}
