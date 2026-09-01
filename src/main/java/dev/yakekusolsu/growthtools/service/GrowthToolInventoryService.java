package dev.yakekusolsu.growthtools.service;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** Locates the original UUID-bearing tool after a delayed gameplay action. */
public final class GrowthToolInventoryService {
    private final GrowthToolItemService itemService;

    public GrowthToolInventoryService(GrowthToolItemService itemService) {
        this.itemService = itemService;
    }

    public Optional<ItemStack> find(
            PlayerInventory inventory, UUID toolId, GrowthToolType expectedType) {
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                continue;
            }
            Optional<GrowthToolData> data = itemService.read(item);
            if (data.isPresent()
                    && data.get().toolId().equals(toolId)
                    && data.get().type() == expectedType) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
}
