package dev.yakekusolsu.growthtools.service;

/** Calculates levels from total accumulated experience without Paper dependencies. */
public final class LevelingService {
    private final long experiencePerLevel;
    private final int maximumLevel;

    public LevelingService(long experiencePerLevel, int maximumLevel) {
        if (experiencePerLevel < 1) {
            throw new IllegalArgumentException("experiencePerLevel must be positive");
        }
        if (maximumLevel < 1) {
            throw new IllegalArgumentException("maximumLevel must be positive");
        }
        this.experiencePerLevel = experiencePerLevel;
        this.maximumLevel = maximumLevel;
    }

    /** Returns the EXP needed to advance from {@code level}; zero means it is the maximum level. */
    public long getRequiredExperienceForLevel(int level) {
        validateLevel(level);
        if (level == maximumLevel) {
            return 0;
        }
        return saturatedMultiply(level, experiencePerLevel);
    }

    /** Returns the level represented by non-negative total accumulated EXP. */
    public int calculateLevel(long totalExperience) {
        if (totalExperience < 0) {
            throw new IllegalArgumentException("totalExperience must not be negative");
        }

        int low = 1;
        int high = maximumLevel;
        while (low < high) {
            int middle = low + (high - low + 1) / 2;
            if (hasRequiredTotalExperience(middle, totalExperience)) {
                low = middle;
            } else {
                high = middle - 1;
            }
        }
        return low;
    }

    /** Returns the minimum total accumulated EXP required to reach {@code level}. */
    public long getTotalExperienceForLevel(int level) {
        validateLevel(level);
        long previousLevels = level - 1L;
        // Divide one factor first so the triangular-number calculation stays exact longer.
        long firstFactor = previousLevels;
        long secondFactor = level;
        if ((firstFactor & 1L) == 0L) {
            firstFactor /= 2L;
        } else {
            secondFactor /= 2L;
        }
        return saturatedMultiply(saturatedMultiply(firstFactor, secondFactor), experiencePerLevel);
    }

    public long getExperienceWithinLevel(long totalExperience) {
        int level = calculateLevel(totalExperience);
        return totalExperience - getTotalExperienceForLevel(level);
    }

    public int getMaximumLevel() {
        return maximumLevel;
    }

    private void validateLevel(int level) {
        if (level < 1 || level > maximumLevel) {
            throw new IllegalArgumentException(
                    "level must be between 1 and " + maximumLevel + ": " + level);
        }
    }

    private static long saturatedMultiply(long left, long right) {
        if (left == 0 || right == 0) {
            return 0;
        }
        if (left > Long.MAX_VALUE / right) {
            return Long.MAX_VALUE;
        }
        return left * right;
    }

    private boolean hasRequiredTotalExperience(int level, long totalExperience) {
        long previousLevels = level - 1L;
        long firstFactor = previousLevels;
        long secondFactor = level;
        if ((firstFactor & 1L) == 0L) {
            firstFactor /= 2L;
        } else {
            secondFactor /= 2L;
        }
        if (firstFactor == 0) {
            return true;
        }
        if (firstFactor > totalExperience / secondFactor) {
            return false;
        }
        long triangularNumber = firstFactor * secondFactor;
        return triangularNumber <= totalExperience / experiencePerLevel;
    }
}
