package dev.yakekusolsu.growthtools.storage.database;

import dev.yakekusolsu.growthtools.model.DatabaseStats;
import dev.yakekusolsu.growthtools.model.ToolRegistryStatus;
import dev.yakekusolsu.growthtools.storage.GrowthToolRepository;
import dev.yakekusolsu.growthtools.storage.PlacedBlockRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Owns database lifecycle, async execution, degraded mode, and on-demand statistics. */
public final class DatabaseRuntime implements AutoCloseable {
    private final DatabaseProvider provider;
    private final DatabaseExecutor executor;
    private final SchemaMigrationService migrationService;
    private final GrowthToolRepository toolRepository;
    private final PlacedBlockRepository placedBlockRepository;
    private final Logger logger;
    private volatile boolean ready;

    public DatabaseRuntime(
            DatabaseProvider provider,
            DatabaseExecutor executor,
            SchemaMigrationService migrationService,
            GrowthToolRepository toolRepository,
            PlacedBlockRepository placedBlockRepository,
            Logger logger) {
        this.provider = provider;
        this.executor = executor;
        this.migrationService = migrationService;
        this.toolRepository = toolRepository;
        this.placedBlockRepository = placedBlockRepository;
        this.logger = logger;
    }

    public CompletableFuture<Void> start() {
        return executor.run(() -> {
            try {
                Files.createDirectories(provider.databasePath().getParent());
                try (Connection connection = provider.openConnection()) {
                    migrationService.migrate(connection);
                }
                ready = true;
                logger.info("SQLite registry ready at " + provider.databasePath());
            } catch (IOException | SQLException exception) {
                ready = false;
                logger.log(Level.SEVERE,
                        "SQLite initialization failed. PDC tools continue, but persistent placed-block "
                                + "tracking and registry features are unavailable. Block EXP is disabled.",
                        exception);
                throw new CompletionException(exception);
            }
        });
    }

    public CompletableFuture<Void> run(SqlRunnable operation, String description) {
        return executor.run(() -> {
            ensureReady();
            try {
                operation.run();
            } catch (SQLException exception) {
                enterDegradedMode(description, exception);
                throw new CompletionException(exception);
            }
        });
    }

    public <T> CompletableFuture<T> supply(SqlSupplier<T> operation, String description) {
        return executor.supply(() -> {
            ensureReady();
            try {
                return operation.get();
            } catch (SQLException exception) {
                enterDegradedMode(description, exception);
                throw new CompletionException(exception);
            }
        });
    }

    public CompletableFuture<DatabaseStats> stats() {
        return supply(() -> {
            try (Connection connection = provider.openConnection()) {
                int schemaVersion = migrationService.currentVersion(connection);
                long fileSize = Files.exists(provider.databasePath())
                        ? Files.size(provider.databasePath()) : 0L;
                return new DatabaseStats(
                        true,
                        schemaVersion,
                        toolRepository.count(),
                        toolRepository.countByStatus(ToolRegistryStatus.DUPLICATE),
                        placedBlockRepository.count(),
                        fileSize);
            } catch (IOException exception) {
                throw new SQLException("Could not read database file size", exception);
            }
        }, "collect database statistics").exceptionally(exception ->
                new DatabaseStats(false, 0, 0, 0, 0, 0));
    }

    public boolean isReady() {
        return ready;
    }

    public DatabaseProvider provider() {
        return provider;
    }

    private void ensureReady() {
        if (!ready) {
            throw new CompletionException(new IllegalStateException("Database is not ready"));
        }
    }

    private void enterDegradedMode(String description, SQLException exception) {
        ready = false;
        logger.log(Level.WARNING,
                "Database operation failed and SQLite entered degraded mode: " + description
                        + ". PDC tools continue, but block EXP and database-backed features "
                        + "remain disabled until a clean restart.",
                exception);
    }

    @Override
    public void close() {
        executor.close();
        provider.close();
        ready = false;
    }

    @FunctionalInterface
    public interface SqlRunnable {
        void run() throws SQLException;
    }

    @FunctionalInterface
    public interface SqlSupplier<T> {
        T get() throws SQLException;
    }
}
