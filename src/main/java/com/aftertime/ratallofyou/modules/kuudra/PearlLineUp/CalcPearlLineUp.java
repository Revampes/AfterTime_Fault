package com.aftertime.ratallofyou.modules.kuudra.PearlLineUp;


import com.aftertime.ratallofyou.UI.config.ConfigData.AllConfig;
import com.aftertime.ratallofyou.UI.config.ConfigData.ModuleInfo;
import com.aftertime.ratallofyou.utils.KuudraUtils;
import com.aftertime.ratallofyou.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Arrays;
import java.util.List;
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

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        // Check both module enabled and phase 1
        if (!isModuleEnabled() || !KuudraUtils.isPhase(1) || mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // Compute from player eye position
        Vec3 eye = mc.thePlayer.getPositionEyes(event.partialTicks);
        if (eye == null) return;

        // Find nearby supply armor stands (default everything on)
        List<Vec3> supplies = KuudraUtils.findNearbySupplies(200.0);
        if (supplies.isEmpty()) return;

        long now = System.currentTimeMillis();
        boolean hasStart = tracking && progressStartMs > 0;
        long elapsed = hasStart ? (now - progressStartMs) : -1;
        boolean approximate = !hasStart; // mark countdown as approximate if we don’t have a true start time

        Long bestCountdown = null;
        boolean bestApprox = approximate;

        for (Vec3 targetStand : supplies) {
            String spot = SupplyPickUpSpot.getClosestSpot(targetStand)
                    .map(SupplyPickUpSpot::getDisplayText)
                    .orElse("");

            // Find nearest landing coordinate for the pearl
            Vec3 landing = Arrays.stream(KuudraSupplySpot.values())
                    .map(KuudraSupplySpot::getLocation)
                    .min((a, b) -> Double.compare(a.squareDistanceTo(targetStand), b.squareDistanceTo(targetStand)))
                    .orElse(targetStand);

            // Solve both sky and flat to the landing coordinate
            PearlThrowPlan sky = EnderPearlSolver.solve(true, eye, landing);
            PearlThrowPlan flat = EnderPearlSolver.solve(false, eye, landing);

            // Render a subtle beacon at the detected supply stand (for context)
            RenderUtils.renderBeaconBeam(targetStand, new RenderUtils.Color(0, 200, 255, 90), true, 2.0f, event.partialTicks);

            if (sky != null) {
                long throwIn = computeThrowCountdown(elapsed, sky.flightTimeMs);
                if (bestCountdown == null || throwIn < bestCountdown) { bestCountdown = throwIn; bestApprox = approximate; }
                drawAimMarker(sky.aimPoint, 0.45, 0, 255, 0, (spot.isEmpty()?"":"["+spot+"] ") + "Sky", sky, throwIn, approximate);
            }
            if (flat != null) {
                long throwIn = computeThrowCountdown(elapsed, flat.flightTimeMs);
                if (bestCountdown == null || throwIn < bestCountdown) { bestCountdown = throwIn; bestApprox = approximate; }
                drawAimMarker(flat.aimPoint, 0.35, 255, 210, 0, (spot.isEmpty()?"":"["+spot+"] ") + "Flat", flat, throwIn, approximate);
            }
        }

        lastHudCountdownMs = bestCountdown;
        lastHudApproximate = bestApprox;
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

        String timeInfo = String.format("Y%.1f P%.1f  %dms", plan.yaw, plan.pitch, plan.flightTimeMs);
        String countdownInfo;
        if (throwInMs <= 0) countdownInfo = "  THROW NOW";
        else countdownInfo = String.format("  %sThrow in %dms", approximate ? "~" : "", throwInMs);
        String text = String.format("%s  %s%s", label, timeInfo, countdownInfo);
        RenderUtils.renderFloatingText(text, pos.xCoord, pos.yCoord + size + 0.2, pos.zCoord, 1.0f, 0xFFFFFFFF, false);
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!isModuleEnabled() || !KuudraUtils.isPhase(1) || mc.theWorld == null) return;

        // Scan armor stands for progress percent updates
        boolean foundProgress = false;
        int parsedPercent = -1;
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (!(e instanceof EntityArmorStand)) continue;
            String name = ((EntityArmorStand) e).getDisplayName().getUnformattedText();
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
    public void onWorldUnload(WorldEvent.Unload event) {
        tracking = false;
        progressStartMs = -1;
        lastPercent = -1;
        lastHudCountdownMs = null;
        lastHudApproximate = false;
    }

    private boolean isModuleEnabled() {
        ModuleInfo cfg = (ModuleInfo) AllConfig.INSTANCE.MODULES.get("kuudra_pearllineups");
        return cfg != null && Boolean.TRUE.equals(cfg.Data);
    }
}
