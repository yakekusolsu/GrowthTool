package dev.yakekusolsu.growthtools.ability;

public final class AbilitySafetyLimits {
    public static final int MAX_VEIN_BLOCKS = 128;
    public static final int MAX_AREA_RADIUS = 3;
    public static final double MAX_EXPERIENCE_MULTIPLIER = 100.0D;

    private AbilitySafetyLimits() {
    }

    public static int veinBlocks(int requested) {
        return Math.clamp(requested, 1, MAX_VEIN_BLOCKS);
    }

    public static int areaRadius(int requested) {
        return Math.clamp(requested, 0, MAX_AREA_RADIUS);
    }

    public static double experienceMultiplier(double requested, double fallback) {
        if (!Double.isFinite(requested) || requested <= 0.0D) {
            return fallback;
        }
        return Math.min(requested, MAX_EXPERIENCE_MULTIPLIER);
    }
}
