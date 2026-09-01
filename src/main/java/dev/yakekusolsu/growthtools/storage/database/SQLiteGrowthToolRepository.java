package dev.yakekusolsu.growthtools.storage.database;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.model.RegisteredTool;
import dev.yakekusolsu.growthtools.model.ToolRegistryStatus;
import dev.yakekusolsu.growthtools.storage.GrowthToolRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class SQLiteGrowthToolRepository implements GrowthToolRepository {
    private final DatabaseProvider provider;

    public SQLiteGrowthToolRepository(DatabaseProvider provider) {
        this.provider = provider;
    }

    @Override
    public void upsert(RegisteredTool tool) throws SQLException {
        String sql = """
                INSERT INTO growth_tools (
                    tool_id, tool_type, last_known_level, last_known_experience, data_version,
                    first_seen_at, last_seen_at, last_owner_uuid, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(tool_id) DO UPDATE SET
                    tool_type = excluded.tool_type,
                    last_known_level = excluded.last_known_level,
                    last_known_experience = excluded.last_known_experience,
                    data_version = excluded.data_version,
                    last_seen_at = excluded.last_seen_at,
                    last_owner_uuid = excluded.last_owner_uuid,
                    status = CASE
                        WHEN growth_tools.status = 'REPLACED' AND excluded.status = 'ACTIVE'
                        THEN 'DUPLICATE'
                        WHEN growth_tools.status = 'DUPLICATE' AND excluded.status = 'ACTIVE'
                        THEN 'DUPLICATE'
                        ELSE excluded.status
                    END
                """;
        try (Connection connection = provider.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, tool);
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<RegisteredTool> find(UUID toolId) throws SQLException {
        try (Connection connection = provider.openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM growth_tools WHERE tool_id = ?")) {
            statement.setString(1, toolId.toString());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(read(result)) : Optional.empty();
            }
        }
    }

    @Override
    public void updateStatus(UUID toolId, ToolRegistryStatus status, long seenAt)
            throws SQLException {
        try (Connection connection = provider.openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE growth_tools SET status = ?, last_seen_at = ? WHERE tool_id = ?")) {
            statement.setString(1, status.name());
            statement.setLong(2, seenAt);
            statement.setString(3, toolId.toString());
            statement.executeUpdate();
        }
    }

    @Override
    public long count() throws SQLException {
        return countQuery("SELECT COUNT(*) FROM growth_tools", null);
    }

    @Override
    public long countByStatus(ToolRegistryStatus status) throws SQLException {
        return countQuery("SELECT COUNT(*) FROM growth_tools WHERE status = ?", status.name());
    }

    private long countQuery(String sql, String value) throws SQLException {
        try (Connection connection = provider.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            if (value != null) {
                statement.setString(1, value);
            }
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : 0L;
            }
        }
    }

    private static void bind(PreparedStatement statement, RegisteredTool tool) throws SQLException {
        statement.setString(1, tool.toolId().toString());
        statement.setString(2, tool.toolType().name());
        statement.setInt(3, tool.lastKnownLevel());
        statement.setLong(4, tool.lastKnownExperience());
        statement.setInt(5, tool.dataVersion());
        statement.setLong(6, tool.firstSeenAt());
        statement.setLong(7, tool.lastSeenAt());
        statement.setString(8,
                tool.lastOwnerUuid() == null ? null : tool.lastOwnerUuid().toString());
        statement.setString(9, tool.status().name());
    }

    private static RegisteredTool read(ResultSet result) throws SQLException {
        String owner = result.getString("last_owner_uuid");
        return new RegisteredTool(
                UUID.fromString(result.getString("tool_id")),
                GrowthToolType.valueOf(result.getString("tool_type")),
                result.getInt("last_known_level"),
                result.getLong("last_known_experience"),
                result.getInt("data_version"),
                result.getLong("first_seen_at"),
                result.getLong("last_seen_at"),
                owner == null ? null : UUID.fromString(owner),
                ToolRegistryStatus.valueOf(result.getString("status")));
    }
}
