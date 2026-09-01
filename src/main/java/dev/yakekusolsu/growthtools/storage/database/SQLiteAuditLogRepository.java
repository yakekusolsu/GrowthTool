package dev.yakekusolsu.growthtools.storage.database;

import dev.yakekusolsu.growthtools.storage.AuditLogRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public final class SQLiteAuditLogRepository implements AuditLogRepository {
    private final DatabaseProvider provider;

    public SQLiteAuditLogRepository(DatabaseProvider provider) {
        this.provider = provider;
    }

    @Override
    public void record(
            String operation, UUID toolId, UUID actorId, String details, long timestamp)
            throws SQLException {
        try (Connection connection = provider.openConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO audit_log(operation, tool_id, actor_uuid, details, created_at)
                        VALUES (?, ?, ?, ?, ?)
                        """)) {
            statement.setString(1, operation);
            statement.setString(2, toolId == null ? null : toolId.toString());
            statement.setString(3, actorId == null ? null : actorId.toString());
            statement.setString(4, details);
            statement.setLong(5, timestamp);
            statement.executeUpdate();
        }
    }
}
