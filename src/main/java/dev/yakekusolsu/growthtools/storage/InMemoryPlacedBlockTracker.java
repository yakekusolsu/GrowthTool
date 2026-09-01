package dev.yakekusolsu.growthtools.storage;

import dev.yakekusolsu.growthtools.model.PlacedBlockKey;
import dev.yakekusolsu.growthtools.model.PlacedBlockRecord;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.block.Block;

/** Phase 3 in-memory tracker behind a persistence-ready interface. */
public final class InMemoryPlacedBlockTracker implements PlacedBlockTracker {
    private final Map<PlacedBlockKey, PlacedBlockRecord> placedBlocks = new HashMap<>();

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public Optional<PlacedBlockRecord> find(Block block) {
        return Optional.ofNullable(placedBlocks.get(key(block)));
    }

    @Override
    public void markPlaced(Block block, UUID playerId) {
        PlacedBlockKey key = key(block);
        placedBlocks.put(key, new PlacedBlockRecord(key, System.currentTimeMillis(), playerId));
    }

    @Override
    public void restore(Block block, PlacedBlockRecord record) {
        PlacedBlockKey key = key(block);
        placedBlocks.put(key, new PlacedBlockRecord(key, record.placedAt(), record.playerId()));
    }

    @Override
    public void unmark(Block block) {
        placedBlocks.remove(key(block));
    }

    @Override
    public void move(Block from, Block to) {
        PlacedBlockRecord record = placedBlocks.remove(key(from));
        if (record != null) {
            restore(to, record);
        }
    }

    private static PlacedBlockKey key(Block block) {
        return new PlacedBlockKey(
                block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }
}
