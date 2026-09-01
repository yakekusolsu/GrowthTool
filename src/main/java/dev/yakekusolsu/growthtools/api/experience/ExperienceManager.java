package dev.yakekusolsu.growthtools.api.experience;

import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Safe progression mutation. Must be called on the server main thread. */
public interface ExperienceManager {
    /**
     * Adds positive EXP through the full GrowthTools update pipeline.
     * @return empty when {@code tool} is not a valid GrowthTool
     * @throws IllegalArgumentException for non-positive amounts or an invalid source
     * @throws IllegalStateException off the main thread or after plugin disable
     */
    Optional<ExperienceChangeResult> addExperience(
            Player player, ItemStack tool, long amount, ExperienceSourceId source);
}
