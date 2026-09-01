package dev.yakekusolsu.growthtools.storage;

import dev.yakekusolsu.growthtools.model.PlacedBlockRecord;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.block.Block;

/** Boundary for replaceable placed-block tracking implementations. */
public interface PlacedBlockTracker {
    boolean isReady();

    Optional<PlacedBlockRecord> find(Block block);

    default boolean isPlayerPlaced(Block block) {
        return find(block).isPresent();
    }

    void markPlaced(Block block, UUID playerId);

    void restore(Block block, PlacedBlockRecord record);

    void unmark(Block block);

    void move(Block from, Block to);
}
