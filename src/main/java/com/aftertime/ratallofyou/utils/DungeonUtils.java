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

    public static void init(Runnable callback) {
        MinecraftForge.EVENT_BUS.register(new DungeonUtils());
        onDungeonEndCallback = callback;
    }

    public static boolean isInDungeon() {
        // Check both chat-based detection and scoreboard-based detection
        return inDungeon || checkScoreboardForDungeon();
    }

    public static int getDungeonFloor() {
        return dungeonFloor;
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

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();

        if (message.contains("Starting in") ||
                message.contains("Here, I found this map when I first entered the dungeon.") ||
                message.contains("The Catacombs (Floor-")) {
            inDungeon = true;
            dungeonFloor = parseFloor(message);
        }
        else if (message.contains("Dungeon failed") ||
                message.contains("Dungeon completed")) {
            endDungeon();
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