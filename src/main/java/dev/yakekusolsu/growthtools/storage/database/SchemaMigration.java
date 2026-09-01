package dev.yakekusolsu.growthtools.storage.database;

import java.sql.Connection;
import java.sql.SQLException;

/** One atomic forward-only database schema migration. */
public interface SchemaMigration {
    int fromVersion();

    int toVersion();

    void migrate(Connection connection) throws SQLException;
}
