package dev.yakekusolsu.growthtools.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlacedBlockKeyTest {
    @Test
    void equalityIncludesWorldAndExactCoordinates() {
        UUID world = UUID.randomUUID();
        PlacedBlockKey first = new PlacedBlockKey(world, 31, 64, -17);

        assertEquals(first, new PlacedBlockKey(world, 31, 64, -17));
        assertNotEquals(first, new PlacedBlockKey(world, 31, 65, -17));
        assertNotEquals(first, new PlacedBlockKey(UUID.randomUUID(), 31, 64, -17));
        assertEquals(1, first.chunkX());
        assertEquals(-2, first.chunkZ());
    }
}
