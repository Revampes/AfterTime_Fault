package com.aftertime.ratallofyou.modules.kuudra.PhaseOne.PearlLineUp;

import net.minecraft.util.Vec3;

public enum KuudraSupplySpot {
    SUPPLY1(new Vec3(-94.0, 79.0, -106.0)),
    SUPPLY2(new Vec3(-106.0, 79.0, -112.94)),
    SUPPLY3(new Vec3(-98.0, 79.0, -99.06)),
    SUPPLY4(new Vec3(-106.0, 79.0, -99.06)),
    SUPPLY5(new Vec3(-98.0, 79.0, -112.94)),
    SUPPLY6(new Vec3(-110.0, 79.0, -106.0));

    private final Vec3 location;

    KuudraSupplySpot(Vec3 location) {
        this.location = location;
        }

    public Vec3 getLocation() {
        return location;
    }
}

