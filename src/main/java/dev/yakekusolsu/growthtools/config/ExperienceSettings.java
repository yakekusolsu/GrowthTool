package dev.yakekusolsu.growthtools.config;

import java.util.Map;
import org.bukkit.Material;

/** Immutable, in-memory gameplay EXP settings used by high-frequency listeners. */
public record ExperienceSettings(
        boolean creativeMode,
        Map<Material, Long> blockExperience,
        boolean fishingEnabled,
        long fishingExperience,
        boolean bowEnabled,
        long bowHitExperience) {

    public ExperienceSettings {
        blockExperience = Map.copyOf(blockExperience);
        if (fishingExperience < 0 || bowHitExperience < 0) {
            throw new IllegalArgumentException("Configured experience must not be negative");
        }
    }

    public long blockExperience(Material material) {
        return blockExperience.getOrDefault(material, 0L);
    }
}
