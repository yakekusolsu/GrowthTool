package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VeinTraversalTest {
    private final VeinTraversal traversal = new VeinTraversal();
    private final BlockPosition origin = new BlockPosition(0, 0, 0);

    @Test void handlesSingleAndOrthogonalChain() {
        assertEquals(1, traversal.find(origin, Set.of(origin)::contains, false, 16).size());
        Set<BlockPosition> chain = Set.of(origin, new BlockPosition(1, 0, 0),
                new BlockPosition(2, 0, 0));
        assertEquals(3, traversal.find(origin, chain::contains, false, 16).size());
    }

    @Test void diagonalConnectivityCanBeDisabledOrEnabled() {
        Set<BlockPosition> diagonal = Set.of(origin, new BlockPosition(1, 1, 0));
        assertEquals(1, traversal.find(origin, diagonal::contains, false, 16).size());
        assertEquals(2, traversal.find(origin, diagonal::contains, true, 16).size());
    }

    @Test void respectsRequestedAndHardCapsOnLargeCyclicGraphs() {
        Set<BlockPosition> cube = new HashSet<>();
        for (int x = -10; x <= 10; x++) for (int y = -10; y <= 10; y++)
            for (int z = -10; z <= 10; z++) cube.add(new BlockPosition(x, y, z));
        assertEquals(5, traversal.find(origin, cube::contains, true, 5).size());
        assertEquals(AbilitySafetyLimits.MAX_VEIN_BLOCKS,
                traversal.find(origin, cube::contains, true, 10_000).size());
    }

    @Test void returnsEmptyWhenOriginDoesNotMatch() {
        assertTrue(traversal.find(origin, ignored -> false, true, 16).isEmpty());
    }
}
