package dev.yakekusolsu.growthtools.config;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;

/** Immutable reload snapshot for the four Phase 5 built-in abilities. */
public record AbilitySettings(
        VeinMiner veinMiner,
        AreaBreak areaBreak,
        AutoSmelt autoSmelt,
        ExperienceBoost experienceBoost,
        boolean creativeMode,
        boolean loreEnabled,
        int loreMaximumEntries) {
    public record VeinMiner(
            boolean enabled,
            int unlockLevel,
            Duration cooldown,
            boolean requireSneak,
            int maximumBlocks,
            boolean diagonal,
            double extraExperienceMultiplier,
            Map<String, Set<Material>> oreGroups) {
        public VeinMiner {
            oreGroups = oreGroups.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                    Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
        }
    }

    public record AreaBreak(
            boolean enabled,
            int unlockLevel,
            Duration cooldown,
            boolean requireSneak,
            int radius,
            double extraExperienceMultiplier) {
    }

    public record AutoSmelt(
            boolean enabled, int unlockLevel, Duration cooldown, boolean disableWithSilkTouch) {
    }

    public record ExperienceBoost(
            boolean enabled, int unlockLevel, Duration cooldown, double multiplier) {
    }
}
