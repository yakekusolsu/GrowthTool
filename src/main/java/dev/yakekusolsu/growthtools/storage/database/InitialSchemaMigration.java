package dev.yakekusolsu.growthtools.storage.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Creates the initial registry, placed-block, and audit schema without deleting existing data. */
public final class InitialSchemaMigration implements SchemaMigration {
    @Override
    public int fromVersion() {
        return 0;
    }

    @Override
    public int toVersion() {
        return 1;
    }

    @Override
    public void migrate(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS growth_tools (
                        tool_id TEXT PRIMARY KEY,
                        tool_type TEXT NOT NULL,
                        last_known_level INTEGER NOT NULL,
                        last_known_experience INTEGER NOT NULL,
                        data_version INTEGER NOT NULL,
                        first_seen_at INTEGER NOT NULL,
                        last_seen_at INTEGER NOT NULL,
                        last_owner_uuid TEXT,
                        status TEXT NOT NULL
                    )
                    """);
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_growth_tools_status ON growth_tools(status)");
            statement.executeUpdate(
                    "CREATE INDEX IF NOT EXISTS idx_growth_tools_owner ON growth_tools(last_owner_uuid)");
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS placed_blocks (
                        world_uuid TEXT NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        z INTEGER NOT NULL,
                        chunk_x INTEGER NOT NULL,
                        chunk_z INTEGER NOT NULL,
                        placed_at INTEGER NOT NULL,
                        player_uuid TEXT NOT NULL,
                        PRIMARY KEY (world_uuid, x, y, z)
                    )
                    """);
            statement.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS idx_placed_blocks_chunk
                    ON placed_blocks(world_uuid, chunk_x, chunk_z)
                    """);
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS audit_log (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        operation TEXT NOT NULL,
                        tool_id TEXT,
                        actor_uuid TEXT,
                        details TEXT NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                    """);
        }
    }
}
