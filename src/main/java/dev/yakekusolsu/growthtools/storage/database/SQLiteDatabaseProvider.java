package dev.yakekusolsu.growthtools.storage.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens independently scoped SQLite connections with safe server defaults. */
public final class SQLiteDatabaseProvider implements DatabaseProvider {
    private final Path databasePath;
    private volatile boolean available;

    public SQLiteDatabaseProvider(Path databasePath) {
        this.databasePath = databasePath.toAbsolutePath().normalize();
    }

    @Override
    public Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA busy_timeout = 5000");
            statement.execute("PRAGMA journal_mode = WAL");
        }
        available = true;
        return connection;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public Path databasePath() {
        return databasePath;
    }

    @Override
    public void close() {
        available = false;
    }
}
