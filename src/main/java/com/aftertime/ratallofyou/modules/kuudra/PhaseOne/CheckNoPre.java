package com.aftertime.ratallofyou.modules.kuudra.PhaseOne;

import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/**
 * CheckNoPre: detects player's pre spot and verifies crates appear at pre and the expected second spot.
 */
public class CheckNoPre {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final double[] SHOP = new double[]{-81, 76, -143};
    private static final double[] XCANNON = new double[]{-143, 76, -125};
    private static final double[] SQUARE = new double[]{-143, 76, -80};
    private static final double[] TRIANGLE = new double[]{-67.5, 77, -122.5};
    private static final double[] X = new double[]{-142.5, 77, -151};
    private static final double[] EQUALS = new double[]{-65.5, 76, -87.5};
    private static final double[] SLASH = new double[]{-113.5, 77, -68.5};

    // Schedule times (ms since epoch) for checks after Elle's message
    private long preCheckAt = 0L;     // t0 + 9s
    private long secondCheckAt = 0L;  // t0 + 11.5s

    private String preSpot = null;
    private double[] preLoc = null;

    // Pending confirmation state
    private boolean pendingConfirm = false;
    private long confirmAt = 0L;
    private String pendingSecondName = null;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            String msg = event.message.getUnformattedText();
            if (msg == null) return;
            if (!msg.contains("Okay adventurers, I will go and fish up Kuudra!")) return;
            if (!isPhase1InKuudra()) return;

            long now = System.currentTimeMillis();
            preCheckAt = now + 9000L;       // 9s
            secondCheckAt = now + 11500L;   // 11.5s
            preSpot = null;
            preLoc = null;
        } catch (Exception ignored) { }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isPhase1InKuudra() || !isModuleEnabled()) return;
        long now = System.currentTimeMillis();

        // Handle deferred confirmation
        if (pendingConfirm && now >= confirmAt) {
            boolean stillMissing = !recheckSecond();
            if ("X".equals(preSpot)) {
                double nd = nearestDist2DTo(XCANNON);
                if (nd >= 0 && nd < 6) {
                    stillMissing = false;
                }
            }
            if (stillMissing && pendingSecondName != null && !pendingSecondName.isEmpty()) {
                sendParty("No " + pendingSecondName + "!");
            }
            pendingConfirm = false;
            confirmAt = 0L;
            pendingSecondName = null;
            preCheckAt = 0L;
            secondCheckAt = 0L;
            return;
        }

        if (preCheckAt != 0L && now >= preCheckAt && preSpot == null) {
            detectPreSpot();
        }
        if (secondCheckAt != 0L && now >= secondCheckAt && !pendingConfirm) {
            verifyCratesAndAnnounce();
            if (!pendingConfirm) {
                preCheckAt = 0L;
                secondCheckAt = 0L;
            }
        }
    }

    private void detectPreSpot() {
        if (mc.thePlayer == null) return;
        // Check in the specific order with given radii
        if (distanceToPlayer(TRIANGLE) < 15) {
            preSpot = "Triangle"; preLoc = TRIANGLE;
        } else if (distanceToPlayer(X) < 30) {
            preSpot = "X"; preLoc = X;
        } else if (distanceToPlayer(EQUALS) < 15) {
            preSpot = "Equals"; preLoc = EQUALS;
        } else if (distanceToPlayer(SLASH) < 15) {
            preSpot = "Slash"; preLoc = SLASH;
        }
    }

    private void verifyCratesAndAnnounce() {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (preSpot == null || preLoc == null) {
            sendParty("Could not determine your pre spot (too far away?)");
            return;
        }

        boolean pre = false;
        boolean second = false;

        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityGiantZombie)) continue;
            EntityGiantZombie g = (EntityGiantZombie) e;
            if (!KuudraUtils.isKuudraCrateGiant(g)) continue;
            // compute crate position
            net.minecraft.util.Vec3 crate = KuudraUtils.cratePos(g);
            double[] c = new double[]{crate.xCoord, crate.yCoord, crate.zCoord};

            if (distance2D(preLoc, c) < 18) pre = true;
            if ("Triangle".equals(preSpot)) {
                if (distance2D(SHOP, c) < 18) second = true;
            } else if ("X".equals(preSpot)) {
                double d = distance2D(XCANNON, c);
                if (d < 16) second = true; // 2D threshold
            } else if ("Slash".equals(preSpot)) {
                if (distance2D(SQUARE, c) < 20) second = true;
            }
        }

        if ("X".equals(preSpot) && nearestDist2DTo(XCANNON) < 6) {
            second = true;
        }

        if (!pre) {
            sendParty("No " + preSpot + "!");
            return;
        }

        if (!second && ("Triangle".equals(preSpot) || "X".equals(preSpot) || "Slash".equals(preSpot))) {
            switch (preSpot) {
                case "Triangle": pendingSecondName = "Shop"; break;
                case "X": pendingSecondName = "X Cannon"; break;
                case "Slash": pendingSecondName = "Square"; break;
                default: pendingSecondName = null; break;
            }
            pendingConfirm = (pendingSecondName != null);
            confirmAt = System.currentTimeMillis() + 1000L;
        }
    }

    private boolean recheckSecond() {
        if (mc.theWorld == null) return false;
        if (preSpot == null) return false;
        boolean second = false;
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityGiantZombie)) continue;
            EntityGiantZombie g = (EntityGiantZombie) e;
            if (!KuudraUtils.isKuudraCrateGiant(g)) continue;
            net.minecraft.util.Vec3 crate = KuudraUtils.cratePos(g);
            double[] c = new double[]{crate.xCoord, crate.yCoord, crate.zCoord};
            if ("Triangle".equals(preSpot)) {
                if (distance2D(SHOP, c) < 18) { second = true; break; }
            } else if ("X".equals(preSpot)) {
                if (distance2D(XCANNON, c) < 16) { second = true; break; }
            } else if ("Slash".equals(preSpot)) {
                if (distance2D(SQUARE, c) < 20) { second = true; break; }
            }
        }
        return second;
    }

    private double nearestDist2DTo(double[] target) {
        if (mc.theWorld == null || target == null) return -1;
        double best = Double.MAX_VALUE;
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityGiantZombie)) continue;
            EntityGiantZombie g = (EntityGiantZombie) e;
            if (!KuudraUtils.isKuudraCrateGiant(g)) continue;
            net.minecraft.util.Vec3 crate = KuudraUtils.cratePos(g);
            double dx = target[0] - crate.xCoord;
            double dz = target[2] - crate.zCoord;
            double d = Math.sqrt(dx*dx + dz*dz);
            if (d < best) best = d;
        }
        return (best == Double.MAX_VALUE) ? -1 : best;
    }

    private double distanceToPlayer(double[] loc) {
        if (mc.thePlayer == null || loc == null) return Double.MAX_VALUE;
        double dx = mc.thePlayer.posX - loc[0];
        double dy = mc.thePlayer.posY - loc[1];
        double dz = mc.thePlayer.posZ - loc[2];
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    private static double distance2D(double[] a, double[] b) {
        double dx = a[0] - b[0];
        double dz = a[2] - b[2];
        return Math.sqrt(dx*dx + dz*dz);
    }

    private void sendParty(String msg) {
        try {
            if (mc.thePlayer != null && msg != null && !msg.isEmpty()) {
                // Final guard for X Cannon false positives (silent)
                if (msg.toLowerCase(java.util.Locale.ROOT).contains("no x cannon")) {
                    double nd = nearestDist2DTo(XCANNON);
                    if (nd >= 0 && nd < 8) {
                        return;
                    }
                }
                mc.thePlayer.sendChatMessage("/pc " + msg);
            }
        } catch (Exception ignored) { }
    }

    private boolean isPhase1InKuudra() {
        return KuudraUtils.isPhase(1) && KuudraUtils.isInKuudraHollow();
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_checknopre");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
