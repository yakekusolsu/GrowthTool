package dev.yakekusolsu.growthtools.storage;

import dev.yakekusolsu.growthtools.model.PlacedBlockKey;
import dev.yakekusolsu.growthtools.model.PlacedBlockRecord;
import dev.yakekusolsu.growthtools.storage.database.DatabaseRuntime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.block.Block;

/** Write-through memory cache backed asynchronously by the placed_blocks table. */
public final class PersistentPlacedBlockTracker implements PlacedBlockTracker {
    private final Map<PlacedBlockKey, PlacedBlockRecord> cache = new ConcurrentHashMap<>();
    private final PlacedBlockRepository repository;
    private final DatabaseRuntime database;
    private final Logger logger;
    private volatile boolean ready;

    public PersistentPlacedBlockTracker(
            PlacedBlockRepository repository, DatabaseRuntime database, Logger logger) {
        this.repository = repository;
        this.database = database;
        this.logger = logger;
        loadCache();
    }

    @Override
    public boolean isReady() {
        return ready && database.isReady();
    }

    @Override
    public Optional<PlacedBlockRecord> find(Block block) {
        return Optional.ofNullable(cache.get(key(block)));
    }

    @Override
    public void markPlaced(Block block, UUID playerId) {
        PlacedBlockKey key = key(block);
        persist(new PlacedBlockRecord(key, System.currentTimeMillis(), playerId));
    }

    @Override
    public void restore(Block block, PlacedBlockRecord record) {
        PlacedBlockKey key = key(block);
        persist(new PlacedBlockRecord(key, record.placedAt(), record.playerId()));
    }

    @Override
    public void unmark(Block block) {
        PlacedBlockKey key = key(block);
        cache.remove(key);
        database.run(() -> repository.delete(key), "delete placed block")
                .exceptionally(this::disableOnFailure);
    }

    @Override
    public void move(Block from, Block to) {
        PlacedBlockKey fromKey = key(from);
        PlacedBlockRecord old = cache.remove(fromKey);
        if (old == null) {
            return;
        }
        PlacedBlockKey toKey = key(to);
        PlacedBlockRecord moved = new PlacedBlockRecord(toKey, old.placedAt(), old.playerId());
        cache.put(toKey, moved);
        database.run(() -> repository.move(fromKey, moved), "move placed block")
                .exceptionally(this::disableOnFailure);
    }

    private void loadCache() {
        database.supply(repository::loadAll, "load placed-block cache")
                .thenAccept(records -> {
                    records.forEach(record -> cache.put(record.key(), record));
                    ready = true;
                    logger.info("Loaded " + records.size() + " persistent placed blocks.");
                })
                .exceptionally(exception -> {
                    ready = false;
                    logger.severe("Persistent placed-block cache could not be loaded; block EXP remains disabled.");
                    return null;
                });
    }

    private void persist(PlacedBlockRecord record) {
        cache.put(record.key(), record);
        database.run(() -> repository.upsert(record), "persist placed block")
                .exceptionally(this::disableOnFailure);
    }

    private static PlacedBlockKey key(Block block) {
        return new PlacedBlockKey(
                block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    private Void disableOnFailure(Throwable exception) {
        ready = false;
        logger.severe("Persistent placed-block write failed; block EXP is disabled to prevent farming.");
        return null;
    }
}
