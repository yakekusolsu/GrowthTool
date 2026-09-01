package dev.yakekusolsu.growthtools.ability;

final class AbilityMath {
    private AbilityMath() { }

    static long scaled(long base, double multiplier) {
        if (base <= 0 || !Double.isFinite(multiplier) || multiplier <= 0) return 0;
        double result = Math.floor(base * multiplier);
        return result >= Long.MAX_VALUE ? Long.MAX_VALUE : (long) result;
    }

    static long saturatingAdd(long left, long right) {
        try { return Math.addExact(left, right); }
        catch (ArithmeticException exception) { return Long.MAX_VALUE; }
    }
}
