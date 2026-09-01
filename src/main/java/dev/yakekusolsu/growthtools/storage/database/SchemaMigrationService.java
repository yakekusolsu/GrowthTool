package dev.yakekusolsu.growthtools.storage.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;

/** Applies a contiguous migration chain and records the schema version transactionally. */
public final class SchemaMigrationService {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    private final List<SchemaMigration> migrations;

    public SchemaMigrationService(List<SchemaMigration> migrations) {
        this.migrations = migrations.stream()
                .sorted(Comparator.comparingInt(SchemaMigration::fromVersion))
                .toList();
    }

    public void migrate(Connection connection) throws SQLException {
        ensureVersionTable(connection);
        int version = currentVersion(connection);
        if (version > CURRENT_SCHEMA_VERSION) {
            throw new SQLException("Database schema " + version + " is newer than supported version "
                    + CURRENT_SCHEMA_VERSION);
        }

        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            while (version < CURRENT_SCHEMA_VERSION) {
                SchemaMigration migration = findMigration(version);
                migration.migrate(connection);
                version = migration.toVersion();
                setVersion(connection, version);
            }
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    public int currentVersion(Connection connection) throws SQLException {
        ensureVersionTable(connection);
        try (Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT version FROM schema_version LIMIT 1")) {
            return result.next() ? result.getInt("version") : 0;
        }
    }

    private SchemaMigration findMigration(int version) throws SQLException {
        return migrations.stream()
                .filter(migration -> migration.fromVersion() == version)
                .findFirst()
                .orElseThrow(() -> new SQLException("No schema migration from version " + version));
    }

    private static void ensureVersionTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS schema_version (
                        version INTEGER NOT NULL
                    )
                    """);
            statement.executeUpdate("""
                    INSERT INTO schema_version(version)
                    SELECT 0 WHERE NOT EXISTS (SELECT 1 FROM schema_version)
                    """);
        }
    }

    private static void setVersion(Connection connection, int version) throws SQLException {
        try (PreparedStatement statement =
                connection.prepareStatement("UPDATE schema_version SET version = ?")) {
            statement.setInt(1, version);
            statement.executeUpdate();
        }
    }
}
