package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/** Records successful player placements for block EXP exploit protection. */
public final class PlacedBlockListener implements Listener {
    private final PlacedBlockTracker tracker;

    public PlacedBlockListener(PlacedBlockTracker tracker) {
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        tracker.markPlaced(event.getBlockPlaced(), event.getPlayer().getUniqueId());
    }
}
