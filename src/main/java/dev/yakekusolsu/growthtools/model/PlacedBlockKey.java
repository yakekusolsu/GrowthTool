package dev.yakekusolsu.growthtools.model;

import java.util.Objects;
import java.util.UUID;

/** Paper-independent persistent identity of one block coordinate. */
public record PlacedBlockKey(UUID worldId, int x, int y, int z) {
    public PlacedBlockKey {
        Objects.requireNonNull(worldId, "worldId");
    }

    public int chunkX() {
        return x >> 4;
    }

    public int chunkZ() {
        return z >> 4;
    }
}
