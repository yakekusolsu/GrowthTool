package dev.yakekusolsu.growthtools.ability;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Bounded, non-recursive traversal used by Vein Miner and unit tests. */
public final class VeinTraversal {
    public List<BlockPosition> find(
            BlockPosition origin,
            Predicate<BlockPosition> matches,
            boolean diagonal,
            int requestedMaximum) {
        int maximum = AbilitySafetyLimits.veinBlocks(requestedMaximum);
        if (!matches.test(origin)) {
            return List.of();
        }
        ArrayDeque<BlockPosition> queue = new ArrayDeque<>();
        Set<BlockPosition> seen = new HashSet<>();
        List<BlockPosition> result = new ArrayList<>(maximum);
        queue.add(origin);
        seen.add(origin);
        while (!queue.isEmpty() && result.size() < maximum) {
            BlockPosition current = queue.removeFirst();
            result.add(current);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        int distance = Math.abs(x) + Math.abs(y) + Math.abs(z);
                        if (distance == 0 || (!diagonal && distance != 1)) {
                            continue;
                        }
                        BlockPosition next = current.offset(x, y, z);
                        if (seen.add(next) && matches.test(next)) {
                            queue.addLast(next);
                        }
                    }
                }
            }
        }
        return List.copyOf(result);
    }
}
