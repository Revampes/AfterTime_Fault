//package com.aftertime.ratallofyou.modules.utils;
//
//
//import com.aftertime.ratallofyou.Main;
//import net.minecraft.client.Minecraft;
//import net.minecraft.util.BlockPos;
//import net.minecraft.util.ChatComponentText;
//
//public class DungeonUtils {
//    public static boolean isFloor7() {
//        return DungeonListener.floor != null && DungeonListener.floor.floorNumber == 7;
//    }
//
//    public static boolean inBossRoom() {
//        return DungeonListener.inBoss;
//    }
//
//    public static void sendDebugMessage(String message) {
//        if (Minecraft.getMinecraft().thePlayer != null) {
//            Minecraft.getMinecraft().thePlayer.addChatMessage(
//                    new ChatComponentText("§a[DungeonUtils] §r" + message)
//            );
//        }
//    }
//
//    public static M7Phases getF7Phase() {
//        if (!isFloor7() || !inBossRoom() || !LocationUtils.isOnHypixel()) {
//            sendDebugMessage("Not in F7 boss room or not on Hypixel");
//            return M7Phases.Unknown;
//        }
//
//        if (Minecraft.getMinecraft().thePlayer == null) {
//            return M7Phases.Unknown;
//        }
//
//        double posY = Minecraft.getMinecraft().thePlayer.posY;
//        sendDebugMessage("Player Y position: " + posY);
//
//        if (posY > 210) return M7Phases.P1;
//        if (posY > 155) return M7Phases.P2;
//        if (posY > 100) return M7Phases.P3;
//        if (posY > 45) return M7Phases.P4;
//        return M7Phases.P5;
//    }
//
//    public enum M7Phases {
//        Unknown("Unknown"),
//        P1("§aPhase 1"),
//        P2("§ePhase 2"),
//        P3("§6Phase 3"),
//        P4("§cPhase 4"),
//        P5("§4Phase 5");
//
//        private final String displayName;
//
//        M7Phases(String displayName) {
//            this.displayName = displayName;
//        }
//
//        public String getDisplayName() {
//            return displayName;
//        }
//    }
//}
