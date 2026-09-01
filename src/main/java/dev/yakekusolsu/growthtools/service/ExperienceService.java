package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.model.ExperienceGain;
import dev.yakekusolsu.growthtools.model.ExperienceResult;
import dev.yakekusolsu.growthtools.model.GrowthToolData;

/** Paper-independent source of truth for safe GrowthTool experience updates. */
public final class ExperienceService {
    private LevelingService levelingService;

    public ExperienceService(LevelingService levelingService) {
        this.levelingService = levelingService;
    }

    public ExperienceResult addExperience(GrowthToolData data, ExperienceGain gain) {
        if (!data.toolId().equals(gain.toolId()) || data.type() != gain.toolType()) {
            throw new IllegalArgumentException("ExperienceGain does not target the supplied tool");
        }

        long maximumExperience = levelingService.getTotalExperienceForLevel(
                levelingService.getMaximumLevel());
        if (data.experience() > maximumExperience) {
            throw new IllegalArgumentException("Tool experience exceeds the configured maximum");
        }

        int oldLevel = levelingService.calculateLevel(data.experience());
        GrowthToolData normalizedOldData = data.level() == oldLevel
                ? data
                : data.withExperience(data.experience(), oldLevel);

        if (data.experience() == maximumExperience) {
            return new ExperienceResult(gain, normalizedOldData, normalizedOldData, 0, false);
        }

        long remaining = maximumExperience - data.experience();
        long experienceAdded = Math.min(gain.amount(), remaining);
        long newExperience = data.experience() + experienceAdded;
        int newLevel = levelingService.calculateLevel(newExperience);
        GrowthToolData newData = data.withExperience(newExperience, newLevel);
        boolean reachedMaximum = oldLevel < levelingService.getMaximumLevel()
                && newLevel == levelingService.getMaximumLevel();

        return new ExperienceResult(
                gain, normalizedOldData, newData, experienceAdded, reachedMaximum);
    }

    public void updateLevelingService(LevelingService newLevelingService) {
        levelingService = newLevelingService;
    }

    public int maximumLevel() {
        return levelingService.getMaximumLevel();
    }

    public long experienceWithinLevel(long totalExperience) {
        return levelingService.getExperienceWithinLevel(totalExperience);
    }

    public long requiredExperience(int level) {
        return levelingService.getRequiredExperienceForLevel(level);
    }

    public long totalExperienceForLevel(int level) {
        return levelingService.getTotalExperienceForLevel(level);
    }
}
