package dev.yakekusolsu.growthtools.model;

import java.util.Objects;
import java.util.UUID;

/** Persistent placed-block audit data. */
public record PlacedBlockRecord(PlacedBlockKey key, long placedAt, UUID playerId) {
    public PlacedBlockRecord {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(playerId, "playerId");
        if (placedAt < 0) {
            throw new IllegalArgumentException("placedAt must not be negative");
        }
    }
}
