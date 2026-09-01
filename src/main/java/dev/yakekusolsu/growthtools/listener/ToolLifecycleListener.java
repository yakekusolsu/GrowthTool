package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.model.ToolRegistryStatus;
import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import dev.yakekusolsu.growthtools.service.ToolRegistryService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/** Performs bounded inventory observations and tracks unambiguous item destruction. */
public final class ToolLifecycleListener implements Listener {
    private final GrowthToolItemService itemService;
    private final ToolRegistryService registryService;

    public ToolLifecycleListener(
            GrowthToolItemService itemService, ToolRegistryService registryService) {
        this.itemService = itemService;
        this.registryService = registryService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        registryService.observeInventory(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            itemService.read(event.getItem().getItemStack())
                    .ifPresent(data -> registryService.observe(data, player, true));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemBreak(PlayerItemBreakEvent event) {
        itemService.read(event.getBrokenItem()).ifPresent(data -> registryService.markStatus(
                data.toolId(),
                ToolRegistryStatus.DESTROYED,
                event.getPlayer().getUniqueId(),
                "Item durability reached zero"));
    }
}
