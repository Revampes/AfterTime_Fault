package com.aftertime.ratallofyou.modules.kuudra.PhaseOne.PearlLineUp;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalcPearlLineUp {
    private static final Minecraft mc = Minecraft.getMinecraft();

    // Countdown defaults (can be made configurable later)
    private static final int DEFAULT_TARGET_DELAY_MS = 6500; // assume Tier 5 window by default
    private static final int SAFETY_BUFFER_MS = 300;         // rotate/safety buffer before throw

    private long progressStartMs = -1;
    private boolean tracking = false;
    private int lastPercent = -1;
    private static final Pattern PROGRESS_PATTERN = Pattern.compile("PROGRESS:\\s*(\\d+)%");

    private Long lastHudCountdownMs = null;
    private boolean lastHudApproximate = false;

    // Cache of which supply spots have already received supplies
    private long lastReceivedScanMs = 0L;
    private boolean[] receivedCache = new boolean[KuudraSupplySpot.values().length];

    // Preferred target spot derived from party chat like "No X!"; if available, prioritize it
    private KuudraSupplySpot preferredSpot = null;

    // Pre spot anchor coordinates (from CheckNoPre)
    private static final double[] SHOP = new double[]{-81, 76, -143};
    private static final double[] XCANNON = new double[]{-143, 76, -125};
    private static final double[] SQUARE = new double[]{-143, 76, -80};
    private static final double[] TRIANGLE = new double[]{-67.5, 77, -122.5};
    private static final double[] X = new double[]{-142.5, 77, -151};
    private static final double[] EQUALS = new double[]{-65.5, 76, -87.5};
    private static final double[] SLASH = new double[]{-113.5, 77, -68.5};

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        // Check both module enabled and phase 1
        if (!isModuleEnabled() || !KuudraUtils.isPhase(1) || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // Recompute received cache at most every 150ms
        long nowMs = System.currentTimeMillis();
        if (nowMs - lastReceivedScanMs > 150) {
            scanReceivedCache();
            lastReceivedScanMs = nowMs;
        }

        // Compute from player eye position
        Vec3 eye = mc.thePlayer.getPositionEyes(event.partialTicks);
        if (eye == null) return;

        Long bestCountdown = null;
        boolean bestApprox = false;

        List<KuudraSupplySpot> available = getAvailableSpots();
        if (available.isEmpty()) {
            lastHudCountdownMs = null;
            lastHudApproximate = false;
            return;
        }

        // Determine targets with priority: chat preferred (only if at Square) > current pre spot > detected stands > all available
        List<KuudraSupplySpot> targets = new ArrayList<>();

        // Are we standing at the Square pre anchor? (Square and XC share SUPPLY6, so check coordinates explicitly)
        boolean atSquare = distanceToPlayer(SQUARE) < 20.0;

        if (atSquare && preferredSpot != null) {
            // Square throws should land on the No-Pre call spot when available
            if (available.contains(preferredSpot)) {
                targets.add(preferredSpot);
            } else {
                // Fallback if that spot already received supplies
                targets.addAll(available);
            }
        } else {
            // Regular behavior for all other pres (XC and others unchanged)
            KuudraSupplySpot prePref = detectCurrentPrePreferred();
            if (prePref != null && available.contains(prePref)) {
                targets.add(prePref);
            } else if (prePref != null && !available.contains(prePref)) {
                targets.addAll(available);
            } else {
                // Regular behavior: for each detected supply stand, choose nearest available landing
                List<Vec3> supplies = KuudraUtils.findNearbySupplies(200.0);
                if (!supplies.isEmpty()) {
                    for (Vec3 targetStand : supplies) {
                        KuudraSupplySpot nearest = nearestAvailableTo(targetStand, available);
                        if (nearest != null && !targets.contains(nearest)) {
                            targets.add(nearest);
                            // Render a subtle beacon at the detected supply stand (for context)
                            RenderUtils.renderBeaconBeam(targetStand, new RenderUtils.Color(0, 200, 255, 90), true, 2.0f, event.partialTicks);
                        }
                    }
                }
                // Fallback: if no stands found or none mapped, show all available
                if (targets.isEmpty()) targets.addAll(available);
            }
        }

        // Render throw plans for selected targets
        for (KuudraSupplySpot spot : targets) {
            Vec3 landing = spot.getLocation();
            PearlThrowPlan sky = EnderPearlSolver.solve(true, eye, landing);
            PearlThrowPlan flat = EnderPearlSolver.solve(false, eye, landing);

            String labelPrefix = spotDisplayName(spot) + (spot.equals(preferredSpot) && atSquare ? "* " : " ");

            if (sky != null) {
                long throwIn = computeThrowCountdown(currentElapsedMs(), sky.flightTimeMs);
                if (bestCountdown == null || throwIn < bestCountdown) { bestCountdown = throwIn; bestApprox = !tracking; }
                drawAimMarker(sky.aimPoint, 0.45, 0, 255, 0, labelPrefix + "Sky", sky, throwIn, !tracking);
            }
            if (flat != null) {
                long throwIn = computeThrowCountdown(currentElapsedMs(), flat.flightTimeMs);
                if (bestCountdown == null || throwIn < bestCountdown) { bestCountdown = throwIn; bestApprox = !tracking; }
                drawAimMarker(flat.aimPoint, 0.35, 255, 210, 0, labelPrefix + "Flat", flat, throwIn, !tracking);
            }
        }

        lastHudCountdownMs = bestCountdown;
        lastHudApproximate = bestApprox;
    }

    private KuudraSupplySpot detectCurrentPrePreferred() {
        if (mc.thePlayer == null) return null;
        // Mirror CheckNoPre ordering (Triangle, X, Equals, Slash), add Square/Shop after
        if (distanceToPlayer(TRIANGLE) < 15) return KuudraSupplySpot.SUPPLY1; // tri -> S1
        if (distanceToPlayer(X) < 30) return KuudraSupplySpot.SUPPLY2;        // X -> S2
        if (distanceToPlayer(EQUALS) < 15) return KuudraSupplySpot.SUPPLY3;   // = -> S3
        if (distanceToPlayer(SLASH) < 15) return KuudraSupplySpot.SUPPLY4;    // / -> S4
        // Additional spots often asked for
        if (distanceToPlayer(SQUARE) < 20) return KuudraSupplySpot.SUPPLY6;   // square -> S6 (fixed)
        if (distanceToPlayer(SHOP) < 20) return KuudraSupplySpot.SUPPLY5;     // shop -> S5
        // Rarely pre but mapped if needed
        if (distanceToPlayer(XCANNON) < 20) return KuudraSupplySpot.SUPPLY6;  // xcannon -> S6 (correct)
        return null;
    }

    private double distanceToPlayer(double[] loc) {
        if (mc.thePlayer == null || loc == null) return Double.MAX_VALUE;
        double dx = mc.thePlayer.posX - loc[0];
        double dy = mc.thePlayer.posY - loc[1];
        double dz = mc.thePlayer.posZ - loc[2];
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    private long currentElapsedMs() {
        long now = System.currentTimeMillis();
        boolean hasStart = tracking && progressStartMs > 0;
        return hasStart ? (now - progressStartMs) : -1;
    }

    private KuudraSupplySpot nearestAvailableTo(Vec3 stand, List<KuudraSupplySpot> available) {
        KuudraSupplySpot best = null; double bestD = Double.MAX_VALUE;
        for (KuudraSupplySpot s : available) {
            double d = s.getLocation().squareDistanceTo(stand);
            if (d < bestD) { bestD = d; best = s; }
        }
        return best;
    }

    private List<KuudraSupplySpot> getAvailableSpots() {
        List<KuudraSupplySpot> list = new ArrayList<>();
        KuudraSupplySpot[] all = KuudraSupplySpot.values();
        for (int i = 0; i < all.length; i++) {
            if (!receivedCache[i]) list.add(all[i]);
        }
        return list;
    }

    private void scanReceivedCache() {
        Arrays.fill(receivedCache, false);
        // For each supply spot, mark received if a matching stand is nearby using centralized helper
        KuudraSupplySpot[] spots = KuudraSupplySpot.values();
        for (int i = 0; i < spots.length; i++) {
            Vec3 loc = spots[i].getLocation();
            receivedCache[i] = KuudraUtils.suppliesReceivedNear(loc, 3.0);
        }
    }

    private String spotDisplayName(KuudraSupplySpot spot) {
        switch (spot) {
            case SUPPLY1: return "[S1]";
            case SUPPLY2: return "[S2]";
            case SUPPLY3: return "[S3]";
            case SUPPLY4: return "[S4]";
            case SUPPLY5: return "[S5]";
            case SUPPLY6: return "[S6]";
            default: return "";
        }
    }

    private long computeThrowCountdown(long elapsedMs, long flightTimeMs) {
        long estElapsed;
        if (elapsedMs >= 0) {
            estElapsed = elapsedMs;
        } else if (lastPercent >= 0) {
            estElapsed = (long) (DEFAULT_TARGET_DELAY_MS * (lastPercent / 100.0));
        } else {
            estElapsed = 0; // best-effort fallback
        }
        return (DEFAULT_TARGET_DELAY_MS - SAFETY_BUFFER_MS) - estElapsed - flightTimeMs;
    }

    private void drawAimMarker(Vec3 pos, double size, int r, int g, int b, String label, PearlThrowPlan plan, long throwInMs, boolean approximate) {
        double hs = size / 2.0;
        double x0 = pos.xCoord - hs;
        double y0 = pos.yCoord;
        double z0 = pos.zCoord - hs;
        double x1 = pos.xCoord + hs;
        double y1 = pos.yCoord + size;
        double z1 = pos.zCoord + hs;

        RenderUtils.renderBoxFromCorners(x0, y0, z0, x1, y1, z1,
                r / 255f, g / 255f, b / 255f, 0.9f, true, 2.0f, true);

        // Show only a concise percentage depending on throw type
        String pctText;
        if (label != null && label.toLowerCase(Locale.ROOT).contains("sky")) pctText = "~38%";
        else if (label != null && label.toLowerCase(Locale.ROOT).contains("flat")) pctText = "~72%";
        else pctText = "~%"; // fallback
        RenderUtils.renderFloatingTextConstant(pctText, pos.xCoord, pos.yCoord + size + 1, pos.zCoord, 0.03f, 0xFFFFFFFF, false);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isModuleEnabled() || !KuudraUtils.isPhase(1) || mc.theWorld == null) return;

        // Scan armor stands for progress percent updates
        boolean foundProgress = false;
        int parsedPercent = -1;
        for (Entity e : mc.theWorld.loadedEntityList) {
            String name = KuudraUtils.getArmorStandName(e);
            if (name == null) continue;
            Matcher m = PROGRESS_PATTERN.matcher(name);
            if (m.find()) {
                foundProgress = true;
                try { parsedPercent = Integer.parseInt(m.group(1)); } catch (NumberFormatException ignored) {}
                break;
            }
        }

        if (foundProgress) {
            long now = System.currentTimeMillis();
            if (!tracking) {
                // Start tracking even mid-progress by back-calculating an approximate start
                long estElapsed = parsedPercent > 0 ? (long) (DEFAULT_TARGET_DELAY_MS * (parsedPercent / 100.0)) : 0L;
                progressStartMs = now - estElapsed;
                tracking = true;
            }
            lastPercent = parsedPercent;
            if (parsedPercent >= 100) {
                tracking = false;
                progressStartMs = -1;
                lastPercent = -1;
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (!isModuleEnabled() || !KuudraUtils.isPhase(1) || mc.thePlayer == null || mc.theWorld == null) {
            lastHudCountdownMs = null; // hide when not applicable
            return;
        }
        if (lastHudCountdownMs == null) return;

        String text;
        int color;
        if (lastHudCountdownMs <= 0) {
            text = "Pearl: THROW NOW";
            color = 0xFFFF5555; // red
        } else {
            text = String.format("Pearl: %sThrow in %dms", lastHudApproximate ? "~" : "", lastHudCountdownMs);
            color = 0xFFFFFF55; // yellow-ish
        }

        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth();
        int x = (w - mc.fontRendererObj.getStringWidth(text)) / 2;
        int y = 8; // top padding
        mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            if (!isModuleEnabled() || !KuudraUtils.isPhase(1)) return;
            String msg = event.message.getUnformattedText();
            if (msg == null) return;
            String lower = msg.toLowerCase(Locale.ROOT);
            // Expect formats like: "Party > name: No X!" or direct "No X!"
            int idx = lower.indexOf(": no ");
            String extract = null;
            if (idx != -1 && msg.endsWith("!")) {
                extract = msg.substring(idx + 5).trim();
            } else if (lower.startsWith("no ") && msg.endsWith("!")) {
                extract = msg.substring(3).trim();
            }
            if (extract != null) {
                if (extract.endsWith("!")) extract = extract.substring(0, extract.length() - 1);
                KuudraSupplySpot mapped = mapNameToSupplySpot(extract);
                if (mapped != null) {
                    preferredSpot = mapped;
                }
            }
        } catch (Exception ignored) { }
    }

    private KuudraSupplySpot mapNameToSupplySpot(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replaceAll("\\s+", " ");
        if (s.equalsIgnoreCase("tri") || s.equalsIgnoreCase("triangle")) return KuudraSupplySpot.SUPPLY1;
        if (s.equalsIgnoreCase("shop")) return KuudraSupplySpot.SUPPLY5;
        if (s.equals("=") || s.equalsIgnoreCase("equals")) return KuudraSupplySpot.SUPPLY3;
        if (s.equals("/") || s.equalsIgnoreCase("slash")) return KuudraSupplySpot.SUPPLY4;
        if (s.equalsIgnoreCase("x")) return KuudraSupplySpot.SUPPLY2;
        if (s.equalsIgnoreCase("xc") || s.equalsIgnoreCase("xcannon") || s.equalsIgnoreCase("x cannon")) return KuudraSupplySpot.SUPPLY6; // include "XC"
        if (s.equalsIgnoreCase("square")) return KuudraSupplySpot.SUPPLY6; // fixed from S5 -> S6
        return null;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        tracking = false;
        progressStartMs = -1;
        lastPercent = -1;
        lastHudCountdownMs = null;
        lastHudApproximate = false;
        Arrays.fill(receivedCache, false);
        preferredSpot = null;
        lastReceivedScanMs = 0L;
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_pearllineups");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}

