package com.divecharter.hub.models.enums;

public enum CertificationLevel {
    OPEN_WATER(18),
    ADVANCED_OPEN_WATER(30),
    RESCUE_DIVER(30),
    DEEP_SPECIALTY(40),
    DIVEMASTER(40);

    private final int maxDepthMeters;

    CertificationLevel(int maxDepthMeters) {
        this.maxDepthMeters = maxDepthMeters;
    }

    public int getMaxDepthMeters() {
        return maxDepthMeters;
    }

    public boolean canDive(int siteDepthMeters) {
        return siteDepthMeters <= maxDepthMeters;
    }
}
