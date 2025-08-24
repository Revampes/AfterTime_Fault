package com.aftertime.ratallofyou.modules.kuudra.PhaseOne.PearlLineUp;

import net.minecraft.util.Vec3;

public class PearlThrowPlan {
    public final Vec3 aimPoint;     // where to aim (world coords)
    public final long flightTimeMs; // approximate flight time
    public final float yaw;         // yaw to aim
    public final float pitch;       // pitch to aim

    public PearlThrowPlan(Vec3 aimPoint, long flightTimeMs, float yaw, float pitch) {
        this.aimPoint = aimPoint;
        this.flightTimeMs = flightTimeMs;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
