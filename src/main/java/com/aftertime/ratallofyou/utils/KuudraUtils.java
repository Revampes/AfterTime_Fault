package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.Vec3;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KuudraUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Phase tracking
    private static int phase = -1;
    private static boolean[] supplies = new boolean[6];
    private static String preSpot = "";
    private static double[] preLoc = new double[3];
    private static String missing = "";
    private static Set<String> freshers = new HashSet<String>();
    private static long freshTime = 0;
    private static int freshLeft = 0;
    private static int build = 0;
    private static int builders = 0;
    private static List<BlockPos> buildPiles = new ArrayList<BlockPos>();

    // Rendering
    private static boolean registered = false;

    public static void init() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(new KuudraUtils());
            registered = true;
        }
    }

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
        String name = stand.getDisplayName() != null ? stand.getDisplayName().getUnformattedText() : null;
        if (name == null || name.isEmpty()) return false;

        String upper = name.toUpperCase();
        if (upper.contains("RECEIVED")) return false;

        return upper.contains("CLICK TO PICK UP") ||
               upper.contains("SUPPLIES") ||
               upper.contains("KUUDRA CRATE") ||
               // Fallback: match any crate label, in case formatting/locale changes
               (upper.contains("CRATE") && upper.contains("KUUDRA"));
    }

    public static boolean isInteractable(Entity entity) {
        return isValidSupply(entity) &&
                mc.thePlayer.getDistanceToEntity(entity) <= 4;
    }

    // Phase management
    public static void reset() {
        supplies = new boolean[6];
        phase = -1;
        preSpot = "";
        preLoc = new double[3];
        missing = "";
        freshers.clear();
        freshTime = 0;
        freshLeft = 0;
        build = 0;
        builders = 0;
        buildPiles.clear();
    }

    public static boolean inKuudra() {
        return phase != -1;
    }

    public static boolean isFight() {
        return phase > 0;
    }

    public static boolean isPhase(int checkPhase) {
        return phase == checkPhase;
    }

    // Chat handlers
    @SubscribeEvent
    public void onChat(net.minecraftforge.client.event.ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();

        if (message.contains("Talk with me to begin!")) {
            phase = -1;
        }
        else if (message.contains("Okay adventurers, I will go and fish up Kuudra!")) {
            phase = 1;
        }
        else if (message.contains("Great work collecting my supplies!")) {
            phase = 2;
        }
        else if (message.contains("Phew! The Ballista is finally ready!")) {
            phase = 3;
            freshers.clear();
        }
        else if (message.contains("POW! SURELY THAT'S IT!")) {
            phase = 4;
        }
    }

    @SubscribeEvent
    public void onGuiClosed(net.minecraftforge.client.event.GuiScreenEvent event) {
        if (inKuudra() && event.gui != null && event.gui.toString().contains("vigilance")) {
            // Handle GUI closed event
        }
    }

    @SubscribeEvent
    public void onWorldUnload(net.minecraftforge.event.world.WorldEvent.Unload event) {
        reset();
    }

    // Rendering
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!inKuudra()) return;

        // Render HP and other elements here
    }

    // Tick handler
    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Handle per-tick updates
    }

    // Helper methods
    public static void drawKuudraHP(String text, double x, double y, double z,
                                    float width, float height, int color, float scale) {
        // Implementation of 3D text rendering would go here
        // This would use similar logic to the JavaScript version but with Minecraft Java methods
    }
}