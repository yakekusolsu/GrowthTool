package dev.yakekusolsu.growthtools.service;

import java.util.List;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

/** Final drops and per-transformer counts from one pipeline pass. */
public record DropTransformationResult(
        List<ItemStack> drops, Map<String, Integer> transformedCounts) {
    public DropTransformationResult {
        drops = List.copyOf(drops);
        transformedCounts = Map.copyOf(transformedCounts);
    }

    public int transformedBy(String transformerId) {
        return transformedCounts.getOrDefault(transformerId, 0);
    }
}
