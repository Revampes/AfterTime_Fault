//package com.aftertime.ratallofyou.modules.utils;
//
//public class LocationUtils {
//    private static boolean isOnHypixel = false;
//    private static Island currentArea = Island.Unknown;
//
//    public static boolean isOnHypixel() {
//        return isOnHypixel;
//    }
//
//    public static Island getCurrentArea() {
//        return currentArea;
//    }
//
//    public static void updateServerStatus(boolean isHypixel) {
//        isOnHypixel = isHypixel;
//    }
//
//    public static void updateLocation(Island area) {
//        currentArea = area;
//    }
//
//    public enum Island {
//        Unknown("Unknown"),
//        Dungeon("Dungeon"),
//        // Other islands omitted for minimal implementation
//        ;
//
//        private final String displayName;
//
//        Island(String displayName) {
//            this.displayName = displayName;
//        }
//
//        public boolean isArea(Island other) {
//            return this == other;
//        }
//    }
//}
