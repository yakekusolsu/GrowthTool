package dev.yakekusolsu.growthtools.ability;

import java.util.ArrayList;
import java.util.List;

public enum AreaPlane {
    XY,
    XZ,
    YZ;

    public List<BlockPosition> positions(BlockPosition center, int requestedRadius) {
        int radius = AbilitySafetyLimits.areaRadius(requestedRadius);
        List<BlockPosition> positions = new ArrayList<>();
        for (int first = -radius; first <= radius; first++) {
            for (int second = -radius; second <= radius; second++) {
                if (first == 0 && second == 0) {
                    continue;
                }
                positions.add(switch (this) {
                    case XY -> center.offset(first, second, 0);
                    case XZ -> center.offset(first, 0, second);
                    case YZ -> center.offset(0, first, second);
                });
            }
        }
        return List.copyOf(positions);
    }
}
