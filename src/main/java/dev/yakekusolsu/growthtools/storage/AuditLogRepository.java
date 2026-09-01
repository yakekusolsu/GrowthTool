package dev.yakekusolsu.growthtools.storage;

import java.sql.SQLException;
import java.util.UUID;

/** Append-only important-operation audit boundary. */
public interface AuditLogRepository {
    void record(String operation, UUID toolId, UUID actorId, String details, long timestamp)
            throws SQLException;
}
