package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/** Validates and restores GrowthTools PDC on common equipment transformation previews. */
public final class ToolTransformationListener implements Listener {
    private final GrowthToolItemService itemService;

    public ToolTransformationListener(GrowthToolItemService itemService) {
        this.itemService = itemService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        preserveFirstGrowthTool(event.getInventory(), event.getResult(), 0, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        preserveFirstGrowthTool(event.getInventory(), event.getResult(), 0, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        preserveFirstGrowthTool(event.getInventory(), event.getResult(), 1);
    }

    private void preserveFirstGrowthTool(
            Inventory inventory, ItemStack result, int... sourceSlots) {
        if (result == null) {
            return;
        }
        for (int slot : sourceSlots) {
            ItemStack source = inventory.getItem(slot);
            if (source != null && itemService.preserveData(source, result)) {
                return;
            }
        }
    }
}
