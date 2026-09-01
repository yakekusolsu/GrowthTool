package dev.yakekusolsu.growthtools.api.internal;

import dev.yakekusolsu.growthtools.api.tool.GrowthToolSnapshot;
import dev.yakekusolsu.growthtools.model.GrowthToolData;
import java.time.Instant;

final class ToolSnapshotMapper {
    private ToolSnapshotMapper() { }
    static GrowthToolSnapshot map(GrowthToolData data, int maximumLevel) {
        return new GrowthToolSnapshot(data.toolId(), data.type(), data.level(), data.experience(),
                Instant.ofEpochMilli(data.createdAt()), data.dataVersion(), maximumLevel);
    }
}
