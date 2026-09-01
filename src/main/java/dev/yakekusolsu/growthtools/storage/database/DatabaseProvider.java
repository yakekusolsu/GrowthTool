package dev.yakekusolsu.growthtools.storage.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

/** Connection boundary that can later be implemented for another SQL database. */
public interface DatabaseProvider extends AutoCloseable {
    Connection openConnection() throws SQLException;

    boolean isAvailable();

    Path databasePath();

    @Override
    void close();
}
