package com.aftertime.ratallofyou.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSkull;
import net.minecraft.util.Vec3;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KuudraUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Phase tracking
    private static int phase = -1;
    private static boolean[] supplies = new boolean[6];
    private static String preSpot = "";
    private static double[] preLoc = new double[3];
    private static String missing = "";
    private static long freshTime = 0; // timestamp when fresh started
    private static int build = 0;
    private static int builders = 0;
    private static List<BlockPos> buildPiles = new ArrayList<BlockPos>();
    // Track time when DPS phase begins (formerly phase 7, now phase 5)
    private static long killTime = 0L;

    // Additional state for PhaseTwo modules
    public static final class StandInfo {
        public final double x, y, z;
        public final String name;
        public StandInfo(double x, double y, double z, String name) {
            this.x = x; this.y = y; this.z = z; this.name = name;
        }
    }
    private static List<StandInfo> buildPilesStands = new ArrayList<StandInfo>();

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
        freshTime = 0;
        build = 0;
        builders = 0;
        buildPiles.clear();
        buildPilesStands.clear();
        killTime = 0L;
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

    // New: phase setter for detection via armor stands
    public static void setPhase(int p) { phase = p; }

    // New: expose current phase and area helper
    public static int getPhase() { return phase; }

    /**
     * Lightweight check if scoreboard indicates we're in Kuudra's Hollow.
     */
    public static boolean isInKuudraHollow() {
        List<String> lines = Utils.getSidebarLines();
        if (lines == null || lines.isEmpty()) return false;
        for (String line : lines) {
            String l = line.toLowerCase(java.util.Locale.ROOT);
            if (l.contains("kuudra") && (l.contains("hollow") || l.contains("kuudra's"))) {
                return true;
            }
            if (Utils.containedByCharSequence(l, "kuudra hollow")) return true;
        }
        return false;
    }

    /**
     * Try to find the Kuudra boss entity in-world by type, size and HP.
     */
    public static EntityMagmaCube findKuudraBoss() {
        if (mc.theWorld == null) return null;
        EntityMagmaCube best = null;
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityMagmaCube)) continue;
            EntityMagmaCube slime = (EntityMagmaCube) e;
            float width = slime.width; // world units
            if (width < 15.0f || width > 18.0f) continue; // heuristic size window
            float hp = slime.getHealth();
            if (hp <= 100_000f) {
                best = slime;
                break; // first match is fine
            }
        }
        return best;
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
        // No-op placeholder
    }

    // Tick handler
    @SubscribeEvent
    public void onTick(ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        // Phase 5 (DPS) detection: when player falls below Y<10 during phase 4
        if (mc.thePlayer != null && isInKuudraHollow()) {
            long yRounded = Math.round(mc.thePlayer.posY);
            if (yRounded < 10 && phase == 4) {
                phase = 5;
                killTime = System.currentTimeMillis();
            }
        }
    }

    // Exposed helpers/state for PhaseTwo modules
    public static int getBuild() { return build; }
    public static void setBuild(int b) { build = b; }

    public static int getBuilders() { return builders; }
    public static void setBuilders(int b) { builders = b; }

    public static long getFreshTime() { return freshTime; }
    public static void setFreshTime(long t) { freshTime = t; }

    public static long getKillTime() { return killTime; }


    public static List<StandInfo> getBuildPilesStands() { return buildPilesStands; }
    public static void setBuildPilesStands(List<StandInfo> list) {
        buildPilesStands = (list == null) ? new ArrayList<StandInfo>() : list;
    }

    // ---------------------- Shared helpers (centralized) ----------------------

    /**
     * Safely obtain the unformatted display name of an Armor Stand entity, or null.
     */
    public static String getArmorStandName(Entity e) {
        if (!(e instanceof EntityArmorStand)) return null;
        try {
            EntityArmorStand as = (EntityArmorStand) e;
            return as.getDisplayName() != null ? as.getDisplayName().getUnformattedText() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Return true if a stand with name containing "SUPPLIES RECEIVED" exists within radius of x/z.
     * The x/z should be the intended center (e.g., tile center at x+0.5, z+0.5).
     */
    public static boolean suppliesReceivedNear(double x, double z, double radius) {
        if (mc.theWorld == null) return false;
        double r2 = radius * radius;
        for (Entity e : mc.theWorld.loadedEntityList) {
            String name = getArmorStandName(e);
            if (name == null) continue;
            String upper = name.toUpperCase(Locale.ROOT);
            if (!upper.contains("SUPPLIES RECEIVED")) continue;
            double dx = e.posX - x;
            double dz = e.posZ - z;
            if (dx * dx + dz * dz <= r2) return true;
        }
        return false;
    }

    public static boolean suppliesReceivedNear(Vec3 pos, double radius) {
        return pos != null && suppliesReceivedNear(pos.xCoord, pos.zCoord, radius);
    }

    /**
     * Normalize various supply names to a canonical set used across modules.
     */
    public static String normalizeSupplyName(String in) {
        if (in == null) return "";
        String s = in.trim();
        s = s.replaceAll("\\s+", " ").trim();
        if (s.equalsIgnoreCase("shop")) return "Shop";
        if (s.equalsIgnoreCase("equals") || s.equalsIgnoreCase("equal") || s.equals("=")) return "Equals";
        if (s.equalsIgnoreCase("x cannon") || s.equalsIgnoreCase("xcannon") || s.equalsIgnoreCase("xc")) return "X Cannon";
        if (s.equalsIgnoreCase("x") || s.equalsIgnoreCase("cross")) return "X";
        if (s.equalsIgnoreCase("triangle") || s.equalsIgnoreCase("tri")) return "Triangle";
        if (s.equalsIgnoreCase("slash") || s.equals("/")) return "Slash";
        if (s.equalsIgnoreCase("square")) return "Square"; // occasionally referenced in chat
        return s;
    }

    /**
     * Detect if an entity is the Giant Zombie used for Kuudra crates (holding a skull/head).
     */
    public static boolean isKuudraCrateGiant(Entity e) {
        if (!(e instanceof EntityGiantZombie)) return false;
        return isKuudraCrateGiant((EntityGiantZombie) e);
    }

    public static boolean isKuudraCrateGiant(EntityGiantZombie giant) {
        try {
            ItemStack held = giant.getHeldItem();
            if (held == null) return false;
            Item item = held.getItem();
            if (item == null) return false;
            if (item instanceof ItemSkull) return true;
            String name = item.getUnlocalizedName();
            return name != null && name.toLowerCase(Locale.ROOT).contains("skull");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Compute the crate position relative to a crate giant's current position/yaw.
     * Uses current tick positions (no interpolation). Y is set to 75.
     */
    public static Vec3 cratePos(EntityGiantZombie giant) {
        double x = giant.posX + (3.7 * Math.cos(Math.toRadians(giant.rotationYaw + 130)));
        double z = giant.posZ + (3.7 * Math.sin(Math.toRadians(giant.rotationYaw + 130)));
        return new Vec3(x, 75.0, z);
    }

    /**
     * Compute the interpolated crate position for smoother rendering.
     */
    public static Vec3 cratePosInterpolated(EntityGiantZombie giant, float partialTicks) {
        double gx = giant.prevPosX + (giant.posX - giant.prevPosX) * partialTicks;
        double gz = giant.prevPosZ + (giant.posZ - giant.prevPosZ) * partialTicks;
        double rad = Math.toRadians(giant.rotationYaw + 130.0);
        double x = gx + 3.7 * Math.cos(rad);
        double z = gz + 3.7 * Math.sin(rad);
        return new Vec3(x, 75.0, z);
    }
}