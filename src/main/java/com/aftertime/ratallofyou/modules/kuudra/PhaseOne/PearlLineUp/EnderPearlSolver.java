package com.aftertime.ratallofyou.modules.kuudra.PhaseOne.PearlLineUp;

import net.minecraft.util.Vec3;

/**
 * Standalone ender pearl trajectory solver (no external config deps).
 * Provides two throw modes:
 * - sky: higher angle, longer range (aim point at SKY_DISTANCE along the throw vector)
 * - flat: lower angle, shorter range (aim point at FLAT_DISTANCE along the throw vector)
 */
public final class EnderPearlSolver {
    private static final double GRAVITY = 0.03;     // vanilla projectile gravity per tick
    private static final double SPEED = 1.5;        // initial speed
    private static final double DRAG = 0.99;        // air drag per tick
    private static final double TICK_MS = 50.0;     // 1 tick = 50 ms

    private static final int MAX_TICKS = 100;
    private static final int REFINE_STEPS = 100;
    private static final int REFINE_ITERATIONS = 50;
    private static final int GRID_STEPS = 100;

    private static final double HIT_RADIUS_SQ = 0.7 * 0.7;  // tolerance around target to count as hit
    private static final double TOLERANCE_SQ = 0.1 * 0.1;   // when to stop refinement

    // Defaults: user asked for everything default on
    private static final int SKY_DISTANCE = 30;   // distance along the vector to render aim point for sky throws
    private static final int FLAT_DISTANCE = 15;  // distance along the vector to render aim point for flat throws

    private static final double MIN_THETA = 0.01;
    private static final double MAX_THETA = Math.PI / 2 - 0.01;
    private static final double INITIAL_REFINE_DEG = 1.0;

    private static final double MIN_SKY_THETA = Math.toRadians(34);
    private static final double MAX_FLAT_THETA = Math.toRadians(32);

    private EnderPearlSolver() {}

    public static PearlThrowPlan solve(boolean sky, Vec3 start, Vec3 target) {
        double dx = target.xCoord - start.xCoord;
        double dz = target.zCoord - start.zCoord;
        double horizontalDist = Math.hypot(dx, dz);

        if (horizontalDist < 1.0) {
            if (!sky) return null;
            // Degenerate case: target basically on top of us; suggest straight-up sky pearl
            Vec3 aim = new Vec3(start.xCoord, start.yCoord + SKY_DISTANCE, start.zCoord);
            return new PearlThrowPlan(aim, 4500, 0.0f, -90.0f);
        }

        double ux = dx / horizontalDist;
        double uz = dz / horizontalDist;

        SearchResult best = initialSweep(start, target, ux, uz, sky);
        if (best == null) return null;

        double refineRange = Math.toRadians(INITIAL_REFINE_DEG);
        for (int iter = 0; iter < REFINE_ITERATIONS; iter++) {
            double lower = Math.max(MIN_THETA, best.theta - refineRange);
            double upper = Math.min(MAX_THETA, best.theta + refineRange);

            SearchResult refined = refineSweep(lower, upper, start, target, ux, uz, sky);
            if (refined == null || refined.errorSq >= best.errorSq) break;

            best = refined;
            if (best.errorSq < TOLERANCE_SQ) break;
            refineRange *= 0.5;
        }

        Vec3 vel = velocity(best.theta, ux, uz);
        Vec3 aimPoint = aimPoint(start, vel, sky ? SKY_DISTANCE : FLAT_DISTANCE);
        long flightTimeMs = Math.round(best.tick * TICK_MS);

        double flatSpeed = Math.hypot(vel.xCoord, vel.zCoord);
        float yaw = (float) (Math.toDegrees(Math.atan2(vel.zCoord, vel.xCoord)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(vel.yCoord, flatSpeed));

        return new PearlThrowPlan(aimPoint, flightTimeMs, yaw, pitch);
    }

    private static SearchResult refineSweep(double minTheta, double maxTheta, Vec3 start, Vec3 target,
                                            double ux, double uz, boolean sky) {
        SearchResult best = null;
        for (int i = 0; i <= REFINE_STEPS; i++) {
            double theta = minTheta + (maxTheta - minTheta) * i / REFINE_STEPS;
            if (sky && theta < MIN_SKY_THETA) continue;
            if (!sky && theta > MAX_FLAT_THETA) continue;
            Vec3 vel = velocity(theta, ux, uz);
            SimResult sim = simulate(start, vel, target);
            if (!sim.hit) continue;
            SearchResult current = new SearchResult(theta, sim.errorSq, sim.hitTick);
            if (isBetter(current, best, sky)) best = current;
        }
        return best;
    }

    private static SearchResult initialSweep(Vec3 start, Vec3 target, double ux, double uz, boolean sky) {
        SearchResult best = null;
        for (int i = 0; i <= GRID_STEPS; i++) {
            double theta = MIN_THETA + (MAX_THETA - MIN_THETA) * i / GRID_STEPS;
            if (sky && theta < MIN_SKY_THETA) continue;
            if (!sky && theta > MAX_FLAT_THETA) continue;
            Vec3 vel = velocity(theta, ux, uz);
            SimResult sim = simulate(start, vel, target);
            if (!sim.hit) continue;
            SearchResult current = new SearchResult(theta, sim.errorSq, sim.hitTick);
            if (isBetter(current, best, sky)) best = current;
        }
        return best;
    }

    private static boolean isBetter(SearchResult a, SearchResult b, boolean sky) {
        if (b == null) return true;
        if (a.errorSq != b.errorSq) return a.errorSq < b.errorSq;
        // Tie-breaker: prefer higher angle for sky; lower angle for flat
        return sky ? a.theta > b.theta : a.theta < b.theta;
    }

    private static SimResult simulate(Vec3 start, Vec3 initialVel, Vec3 target) {
        Vec3 pos = start;
        Vec3 vel = initialVel;

        double bestErrorSq = distSq(pos, target);
        int bestTick = -1;

        double maxDistSq = distSq(start, target) * 4;
        double minY = target.yCoord - 5;

        for (int tick = 0; tick < MAX_TICKS; tick++) {
            pos = pos.addVector(vel.xCoord, vel.yCoord, vel.zCoord);
            double errorSq = distSq(pos, target);

            if (errorSq < bestErrorSq) {
                bestErrorSq = errorSq;
                bestTick = tick;
            }

            if (errorSq < HIT_RADIUS_SQ) return new SimResult(true, errorSq, tick);
            if (pos.yCoord < minY || distSq(start, pos) > maxDistSq) break;

            vel = new Vec3(
                    vel.xCoord * DRAG,
                    (vel.yCoord - GRAVITY) * DRAG,
                    vel.zCoord * DRAG
            );
        }

        return new SimResult(false, bestErrorSq, bestTick);
    }

    private static Vec3 velocity(double theta, double ux, double uz) {
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        return new Vec3(SPEED * cos * ux, SPEED * sin, SPEED * cos * uz);
    }

    private static Vec3 aimPoint(Vec3 start, Vec3 vel, double dist) {
        double norm = vel.lengthVector();
        return new Vec3(
            start.xCoord + (vel.xCoord / norm) * dist,
            start.yCoord + (vel.yCoord / norm) * dist,
            start.zCoord + (vel.zCoord / norm) * dist
        );
    }

    private static double distSq(Vec3 a, Vec3 b) {
        double dx = b.xCoord - a.xCoord;
        double dy = b.yCoord - a.yCoord;
        double dz = b.zCoord - a.zCoord;
        return dx * dx + dy * dy + dz * dz;
    }

    private static final class SimResult {
        final boolean hit; final double errorSq; final int hitTick;
        SimResult(boolean hit, double errorSq, int hitTick) { this.hit = hit; this.errorSq = errorSq; this.hitTick = hitTick; }
    }

    private static final class SearchResult {
        final double theta; final double errorSq; final int tick;
        SearchResult(double theta, double errorSq, int tick) { this.theta = theta; this.errorSq = errorSq; this.tick = tick; }
    }
}
