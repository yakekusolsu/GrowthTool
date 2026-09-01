package dev.yakekusolsu.growthtools.listener;

import dev.yakekusolsu.growthtools.model.PlacedBlockRecord;
import dev.yakekusolsu.growthtools.storage.PlacedBlockTracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/** Keeps placed-block coordinates coherent across common movement and destruction events. */
public final class PlacedBlockLifecycleListener implements Listener {
    private final PlacedBlockTracker tracker;
    private final Map<UUID, PlacedBlockRecord> fallingBlocks = new HashMap<>();

    public PlacedBlockLifecycleListener(PlacedBlockTracker tracker) {
        this.tracker = tracker;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        moveBlocks(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        moveBlocks(event.getBlocks(), event.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplosion(BlockExplodeEvent event) {
        event.blockList().forEach(tracker::unmark);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplosion(EntityExplodeEvent event) {
        event.blockList().forEach(tracker::unmark);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFallingBlockChange(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock fallingBlock)) {
            return;
        }
        if (event.getTo().isAir()) {
            tracker.find(event.getBlock()).ifPresent(record -> {
                tracker.unmark(event.getBlock());
                fallingBlocks.put(fallingBlock.getUniqueId(), record);
            });
        } else {
            PlacedBlockRecord record = fallingBlocks.remove(fallingBlock.getUniqueId());
            if (record != null) {
                tracker.restore(event.getBlock(), record);
            }
        }
    }

    private void moveBlocks(List<Block> blocks, org.bukkit.block.BlockFace direction) {
        List<Block> reverseOrder = new ArrayList<>(blocks);
        Collections.reverse(reverseOrder);
        for (Block block : reverseOrder) {
            tracker.move(block, block.getRelative(direction));
        }
    }
}
