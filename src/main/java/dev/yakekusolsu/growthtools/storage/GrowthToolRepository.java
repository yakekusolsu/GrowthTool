package dev.yakekusolsu.growthtools.storage;

import dev.yakekusolsu.growthtools.model.RegisteredTool;
import dev.yakekusolsu.growthtools.model.ToolRegistryStatus;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/** Storage abstraction for registry and audit projections. */
public interface GrowthToolRepository {
    void upsert(RegisteredTool tool) throws SQLException;

    Optional<RegisteredTool> find(UUID toolId) throws SQLException;

    void updateStatus(UUID toolId, ToolRegistryStatus status, long seenAt) throws SQLException;

    long count() throws SQLException;

    long countByStatus(ToolRegistryStatus status) throws SQLException;
}
