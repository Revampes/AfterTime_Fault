package com.aftertime.ratallofyou.modules.kuudra.PhaseOne;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import com.aftertime.ratallofyou.utils.RenderUtils.Color;
import com.aftertime.ratallofyou.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSkull;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CrateBeaconBeam: renders beacon beams for Kuudra crates and missing supplies during Phase 1.
 * - Draws cyan beams above Kuudra crates (derived from Giant Zombie position and yaw offset).
 * - Draws white beams at supply locations; turns red for the supply called out as missing in party chat.
 */
public class CrateBeaconBeam {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Supply coordinates order must match missing-name mapping
    // Indices: 0 Shop, 1 Equals, 2 X Cannon, 3 X, 4 Triangle, 5 Slash
    private static final int[][] SUPPLY_COORDS = new int[][]{
            {-98, -112},
            {-98, -99},
            {-110, -106},
            {-106, -112},
            {-94, -106},
            {-106, -99}
    };

    // Beam parameters
    private static final float SUPPLY_BEAM_Y = 79f;
    private static final float SUPPLY_BEAM_HEIGHT = 100f;
    private static final float CRATE_BEAM_Y = 75f;
    private static final float CRATE_BEAM_HEIGHT = 100f;

    // State
    private String missingSupplyName = ""; // e.g., "Shop", "X Cannon", etc.
    private final List<Vec3> cratePositions = new ArrayList<>();
    private long lastCrateUpdateMs = 0L;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            if (!isPhase1InKuudra() || !isModuleEnabled()) return;
            String msg = event.message.getUnformattedText();
            // Expect formats like: "Party > name: No Shop!" or "Party > name: No X Cannon!"
            String lower = msg.toLowerCase(Locale.ROOT);
            if (lower.contains("party >") && lower.contains(": no ") && msg.endsWith("!")) {
                int idx = lower.indexOf(": no ");
                if (idx != -1) {
                    String extract = msg.substring(idx + 5).trim(); // after ": no "
                    if (extract.endsWith("!")) extract = extract.substring(0, extract.length() - 1);
                    // Normalize common names to our canonical set
                    String norm = normalizeSupplyName(extract);
                    if (!norm.isEmpty()) missingSupplyName = norm;
                }
            }
        } catch (Exception ignored) { }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        missingSupplyName = "";
        cratePositions.clear();
        lastCrateUpdateMs = 0L;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!isPhase1InKuudra()) return;
        if (mc.theWorld == null || mc.thePlayer == null) return;

        // 1) Update crate positions at most every 100ms to reduce cost
        long now = System.currentTimeMillis();
        if (now - lastCrateUpdateMs > 100) {
            updateCratePositions(event.partialTicks);
            lastCrateUpdateMs = now;
        }

        // 2) Render supply beams (white or red if matches chat-missing)
        for (int i = 0; i < SUPPLY_COORDS.length; i++) {
            int x = SUPPLY_COORDS[i][0];
            int z = SUPPLY_COORDS[i][1];
            if (!isSupplyReceivedAt(x, z)) {
                Color color = getSupplyColorForIndex(i);
                RenderUtils.renderBeaconBeam(new Vec3(x + 0.5, SUPPLY_BEAM_Y, z + 0.5), color, true, SUPPLY_BEAM_HEIGHT, event.partialTicks);
            }
        }

        // 3) Render crate beams (cyan)
        Color cyan = new Color(255, 255, 255, 204);
        for (Vec3 pos : cratePositions) {
            RenderUtils.renderBeaconBeam(new Vec3(pos.xCoord, CRATE_BEAM_Y, pos.zCoord), cyan, true, CRATE_BEAM_HEIGHT, event.partialTicks);
        }
    }

    private void updateCratePositions(float partialTicks) {
        cratePositions.clear();
        if (mc.theWorld == null) return;

        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityGiantZombie)) continue;
            if (!isKuudraCrateGiant((EntityGiantZombie) e)) continue;

            EntityGiantZombie giant = (EntityGiantZombie) e;
            // Interpolate render position
            double gx = giant.prevPosX + (giant.posX - giant.prevPosX) * partialTicks;
            double gz = giant.prevPosZ + (giant.posZ - giant.prevPosZ) * partialTicks;
            float yaw = giant.rotationYaw;
            // Crate position is offset from giant by radius 3.7 at yaw+130 degrees
            double rad = Math.toRadians(yaw + 130.0);
            double x = gx + 3.7 * Math.cos(rad);
            double z = gz + 3.7 * Math.sin(rad);
            cratePositions.add(new Vec3(x, 75.0, z));
        }
    }

    private boolean isKuudraCrateGiant(EntityGiantZombie giant) {
        try {
            ItemStack held = giant.getHeldItem();
            if (held == null) return false;
            Item item = held.getItem();
            if (item == null) return false;
            // 1.8.9: player head is ItemSkull with damage 3; tolerate any skull
            if (item instanceof ItemSkull) return true;
            String name = item.getUnlocalizedName();
            return name != null && name.toLowerCase(Locale.ROOT).contains("skull");
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSupplyReceivedAt(int x, int z) {
        // Detect an armor stand with name containing "SUPPLIES RECEIVED" near the expected X/Z tile
        if (mc.theWorld == null) return false;
        double cx = x + 0.5; // center of tile
        double cz = z + 0.5;
        double radius = 3.0; // allow slight placement offsets
        double r2 = radius * radius;
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityArmorStand)) continue;
            EntityArmorStand stand = (EntityArmorStand) e;
            String name = stand.getDisplayName() != null ? stand.getDisplayName().getUnformattedText() : null;
            if (name == null) continue;
            String upper = name.toUpperCase(Locale.ROOT);
            if (!upper.contains("SUPPLIES RECEIVED")) continue;
            double dx = stand.posX - cx;
            double dz = stand.posZ - cz;
            if (dx * dx + dz * dz <= r2) return true;
        }
        return false;
    }

    private Color getSupplyColorForIndex(int index) {
        String name = nameForSupplyIndex(index);
        boolean highlightRed = !missingSupplyName.isEmpty() && normalizeSupplyName(missingSupplyName).equals(normalizeSupplyName(name));
        return highlightRed ? new Color(255, 0, 0, 255) : new Color(255, 255, 255, 204);
    }

    private static String nameForSupplyIndex(int idx) {
        switch (idx) {
            case 0: return "Shop";
            case 1: return "Equals";
            case 2: return "X Cannon";
            case 3: return "X";
            case 4: return "Triangle";
            case 5: return "Slash";
            default: return "";
        }
    }

    private static String normalizeSupplyName(String in) {
        if (in == null) return "";
        String s = in.trim();
        // unify spacing and case for matching
        s = s.replaceAll("\\s+", " ").trim();
        // canonical names in JS module
        if (s.equalsIgnoreCase("shop")) return "Shop";
        if (s.equalsIgnoreCase("equals")) return "Equals";
        if (s.equalsIgnoreCase("x cannon") || s.equalsIgnoreCase("xcannon")) return "X Cannon";
        if (s.equalsIgnoreCase("x")) return "X";
        if (s.equalsIgnoreCase("triangle")) return "Triangle";
        if (s.equalsIgnoreCase("slash")) return "Slash";
        return s;
    }

    private boolean isPhase1InKuudra() {
        return KuudraUtils.isPhase(1) && isInKuudraHollow();
    }

    // Mimic DungeonUtils scoreboard approach to detect area name
    private boolean isInKuudraHollow() {
        List<String> lines = Utils.getSidebarLines();
        if (lines == null || lines.isEmpty()) return false;
        for (String line : lines) {
            String l = line.toLowerCase(Locale.ROOT);
            if (l.contains("kuudra") && (l.contains("hollow") || l.contains("kuudra's"))) {
                return true;
            }
            // lenient subsequence match
            if (Utils.containedByCharSequence(l, "kuudra hollow")) return true;
        }
        return false;
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_cratebeam");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
