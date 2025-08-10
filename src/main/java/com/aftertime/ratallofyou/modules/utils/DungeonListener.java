//package com.aftertime.ratallofyou.modules.utils;
//
//
//public class DungeonListener {
//    public static boolean isInBossRoom() {
//        if (floor == null) return false;
//
//        double posX = PlayerUtils.getPosX();
//        double posZ = PlayerUtils.getPosZ();
//
//        switch (floor.floorNumber) {
//            case 1:  return posX > -71 && posZ > -39;
//            case 2:
//            case 3:
//            case 4:  return posX > -39 && posZ > -39;
//            case 5:
//            case 6:  return posX > -39 && posZ > -7;
//            case 7:  return posX > -7  && posZ > -7;
//            default: return false;
//        }
//    }
//
//    public static Floor floor = null;
//    public static boolean inBoss = false;
//
//    public static class Floor {
//        public final int floorNumber;
//
//        public Floor(int floorNumber) {
//            this.floorNumber = floorNumber;
//        }
//
//        public static Floor valueOf(String name) {
//            // Implementation to convert floor name to Floor object
//            return new Floor(7); // Simplified for F7
//        }
//    }
//
//    // This would be called from packet handling code when floor is detected
//    public static void setFloor(Floor newFloor) {
//        floor = newFloor;
//        inBoss = isInBossRoom();
//    }
//
//    // This would be called when player position changes
//    public static void updateBossStatus() {
//        inBoss = isInBossRoom();
//    }
//}
