package com.aftertime.ratallofyou.modules.kuudra.PhaseOne.PearlLineUp;

import net.minecraft.util.Vec3;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

public enum SupplyPickUpSpot {
    TRIANGLE(new Vec3(-67.5, 77.0, -122.5), "Triangle"),
    X(new Vec3(-135.5, 77.0, -139.5), "X"),
    EQUALS(new Vec3(-65.5, 76.0, -87.5), "Equals"),
    SLASH(new Vec3(-113.5, 77.0, -68.5), "Slash"),
    SHOP(new Vec3(-77.5, 77.0, -138.5), "Shop"),
    X_CANNON(new Vec3(-135.5, 76.0, -119.5), "X Cannon"),
    SQUARE(new Vec3(-141.5, 77.0, -86.5), "Square"),
    NONE(new Vec3(0.0, 0.0, 0.0), "None");

    private final Vec3 location;
    private final String displayText;

    SupplyPickUpSpot(Vec3 location, String displayText) {
        this.location = location;
        this.displayText = displayText;
    }

    public Vec3 getLocation() {
        return location;
    }

    public String getDisplayText() {
        return displayText;
    }

    public static Optional<SupplyPickUpSpot> getClosestSpot(Vec3 vector) {
        return Arrays.stream(values())
                .filter(spot -> spot != NONE)
                .min(Comparator.comparingDouble(spot -> vector.distanceTo(spot.getLocation())));
    }
}

