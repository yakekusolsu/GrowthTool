package dev.yakekusolsu.growthtools.api.tool;

import java.util.Optional;
import org.bukkit.inventory.ItemStack;

/** Read-only tool lookup. Calls are main-thread-only because ItemStack is mutable Bukkit state. */
public interface ToolManager {
    /** @throws IllegalStateException if called asynchronously or after plugin disable */
    boolean isGrowthTool(ItemStack item);
    /** Returns an immutable snapshot, or empty for invalid/non-GrowthTool state. */
    Optional<GrowthToolSnapshot> getTool(ItemStack item);
}
