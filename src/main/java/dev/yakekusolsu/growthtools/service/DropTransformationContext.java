package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;

/** Immutable tool state available to each drop transformer. */
public record DropTransformationContext(
        GrowthToolType toolType, int toolLevel, ItemStack tool) {
    public DropTransformationContext {
        Objects.requireNonNull(toolType, "toolType");
        Objects.requireNonNull(tool, "tool");
        if (toolLevel < 1) {
            throw new IllegalArgumentException("toolLevel must be positive");
        }
    }
}
