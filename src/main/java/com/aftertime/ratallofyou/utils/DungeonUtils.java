package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;

public class DungeonUtils {
    private static boolean inDungeon = false;
    private static int dungeonFloor = 0;
    private static Runnable onDungeonEndCallback;
    private static boolean wasInWorld = false;
    private static boolean isInP3 = false;

    public static void init(Runnable callback) {
        MinecraftForge.EVENT_BUS.register(new DungeonUtils());
        onDungeonEndCallback = callback;
    }

    public static boolean isInDungeon() {
        // Check both chat-based detection and scoreboard-based detection
        return inDungeon || checkScoreboardForDungeon();
    }

    public static int isInDungeonFloor() {
        // Prefer scoreboard detection; fallback to last known floor from chat
        int floor = checkScoreboardForDungeonFloor();
        if (floor != 0) {
            return floor;
        } else {
            return dungeonFloor;
        }
    }



    public static int getDungeonFloor() {
        return dungeonFloor;
    }

    // Shared helper: check if a module is enabled in config
//    public static boolean isModuleEnabled(String moduleKey) {
//        try {
//            com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo cfg =
//                    (com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo) com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig.INSTANCE.MODULES.get(moduleKey);
//            return cfg != null && Boolean.TRUE.equals(cfg.Data);
//        } catch (Throwable ignored) {
//            return false;
//        }
//    }

    // Shared helper: send a title/subtitle safely
    public static void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.ingameGUI != null) {
            mc.ingameGUI.displayTitle(title, subtitle, fadeIn, stay, fadeOut);
        }

    }

    // Shared helper: clear any current title
    public static void clearTitle() {
        sendTitle("", "", -1, -1, -1);
    }

    private static boolean checkScoreboardForDungeon() {
        List<String> scoreBoardLines = Utils.getSidebarLines();
        if (scoreBoardLines == null || scoreBoardLines.isEmpty()) {
            return false;
        }

        int size = scoreBoardLines.size() - 1;
        for (int i = 0; i < scoreBoardLines.size(); i++) {
            String line = scoreBoardLines.get(size - i).toLowerCase();
            if (Utils.containedByCharSequence(line, "dungeon cleared") ||
                    (Utils.containedByCharSequence(line, "the catacombs") && !line.contains("to"))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInP3() {
        return isInDungeon() && isInDungeonFloor() == 7 && isInP3;

    }

    private static int checkScoreboardForDungeonFloor() {
        List<String> scoreBoardLines = Utils.getSidebarLines();
        if (scoreBoardLines == null || scoreBoardLines.isEmpty()) {
            return 0;
        }

        int size = scoreBoardLines.size() - 1;
        for (int i = 0; i < scoreBoardLines.size(); i++) {
            String line = scoreBoardLines.get(size - i).toLowerCase();
            // Look for occurrences of 'f' followed by a digit 1-7 and use switch-case to map
            for (int idx = line.indexOf('f'); idx != -1; idx = line.indexOf('f', idx + 1)) {
                if (idx + 1 < line.length()) {
                    char c = line.charAt(idx + 1);
                    switch (c) {
                        case '1':
                            return 1;
                        case '2':
                            return 2;
                        case '3':
                            return 3;
                        case '4':
                            return 4;
                        case '5':
                            return 5;
                        case '6':
                            return 6;
                        case '7':
                            return 7;
                        default:
                            break;
                    }
                }
            }
        }
        return 0;
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();

        if (message.contains("Here, I found this map when I first entered the dungeon.")) {
            inDungeon = true;
            dungeonFloor = parseFloor(message);
        }
        else if (message.contains("Dungeon failed") ||
                message.contains("Dungeon completed") ||
                Minecraft.getMinecraft() == null) {
            endDungeon();
        }
        else if (message.equals("[BOSS] Goldor: Who dares trespass into my domain?") ||
                message.equals("[BOSS] Goldor: What do you think you are doing there!") ||
                message.matches("Party > (?:\\[.+\\])? ?(?:.+)?[\u127e\u2692]?: (Bonzo|Phoenix) Procced!?(?: \\(.+\\))?")) {
            isInP3 = true;
        }
        else if (message.equals("The Core entrance is opening!")) {
            isInP3 = false;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null) {
            if (wasInWorld) {
                endDungeon();
            }
            wasInWorld = false;
            return;
        }

        wasInWorld = true;

        // Additional check for when players leave dungeon without chat message
        if (inDungeon && !checkScoreboardForDungeon()) {
            endDungeon();
        }
    }

    private static void endDungeon() {
        boolean wasInDungeon = inDungeon;
        inDungeon = false;
        dungeonFloor = 0;

        if (wasInDungeon && onDungeonEndCallback != null) {
            onDungeonEndCallback.run();
        }
    }

    private static int parseFloor(String message) {
        if (message.contains("Floor I")) return 1;
        if (message.contains("Floor II")) return 2;
        if (message.contains("Floor III")) return 3;
        if (message.contains("Floor IV")) return 4;
        if (message.contains("Floor V")) return 5;
        if (message.contains("Floor VI")) return 6;
        if (message.contains("Floor VII") || message.contains("WELL! WELL! WELL! LOOK WHO'S HERE!")) return 7;
        return 0;
    }
}