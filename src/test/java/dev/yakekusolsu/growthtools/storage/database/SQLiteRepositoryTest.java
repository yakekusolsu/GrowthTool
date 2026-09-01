package dev.yakekusolsu.growthtools.storage.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.yakekusolsu.growthtools.model.GrowthToolType;
import dev.yakekusolsu.growthtools.model.PlacedBlockKey;
import dev.yakekusolsu.growthtools.model.PlacedBlockRecord;
import dev.yakekusolsu.growthtools.model.RegisteredTool;
import dev.yakekusolsu.growthtools.model.ToolRegistryStatus;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.CompletionException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SQLiteRepositoryTest {
    @TempDir
    Path temporaryDirectory;

    private SQLiteDatabaseProvider provider;
    private SchemaMigrationService migrations;

    @BeforeEach
    void initializeDatabase() throws Exception {
        provider = new SQLiteDatabaseProvider(temporaryDirectory.resolve("test.db"));
        migrations = new SchemaMigrationService(List.of(new InitialSchemaMigration()));
        try (Connection connection = provider.openConnection()) {
            migrations.migrate(connection);
        }
    }

    @Test
    void initializesAndRetainsSchemaVersion() throws Exception {
        try (Connection connection = provider.openConnection()) {
            assertEquals(SchemaMigrationService.CURRENT_SCHEMA_VERSION,
                    migrations.currentVersion(connection));
            migrations.migrate(connection);
            assertEquals(1, migrations.currentVersion(connection));
        }
    }

    @Test
    void performsGrowthToolRepositoryCrudAndStatusUpdates() throws Exception {
        SQLiteGrowthToolRepository repository = new SQLiteGrowthToolRepository(provider);
        UUID toolId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        RegisteredTool tool = new RegisteredTool(
                toolId, GrowthToolType.PICKAXE, 2, 100, 1,
                1_000, 2_000, ownerId, ToolRegistryStatus.ACTIVE);

        repository.upsert(tool);
        assertEquals(tool, repository.find(toolId).orElseThrow());
        assertEquals(1, repository.count());

        repository.updateStatus(toolId, ToolRegistryStatus.DUPLICATE, 3_000);
        RegisteredTool duplicate = repository.find(toolId).orElseThrow();
        assertEquals(ToolRegistryStatus.DUPLICATE, duplicate.status());
        assertEquals(3_000, duplicate.lastSeenAt());
        assertEquals(1, repository.countByStatus(ToolRegistryStatus.DUPLICATE));

        repository.upsert(tool);
        assertEquals(ToolRegistryStatus.DUPLICATE,
                repository.find(toolId).orElseThrow().status());
    }

    @Test
    void performsPlacedBlockRepositoryCrudAndMove() throws Exception {
        SQLitePlacedBlockRepository repository = new SQLitePlacedBlockRepository(provider);
        UUID worldId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        PlacedBlockKey first = new PlacedBlockKey(worldId, 1, 64, 1);
        PlacedBlockRecord record = new PlacedBlockRecord(first, 1_000, playerId);

        repository.upsert(record);
        assertEquals(List.of(record), List.copyOf(repository.loadAll()));

        PlacedBlockKey second = new PlacedBlockKey(worldId, 2, 64, 1);
        repository.move(first, new PlacedBlockRecord(second, 1_000, playerId));
        assertEquals(second, repository.loadAll().iterator().next().key());

        repository.delete(second);
        assertTrue(repository.loadAll().isEmpty());
        assertEquals(0, repository.count());
    }

    @Test
    void entersDegradedModeWhenDatabasePathCannotBeCreated() throws Exception {
        Path blockingFile = temporaryDirectory.resolve("not-a-directory");
        java.nio.file.Files.writeString(blockingFile, "block");
        SQLiteDatabaseProvider invalidProvider =
                new SQLiteDatabaseProvider(blockingFile.resolve("growthtools.db"));
        DatabaseRuntime runtime = new DatabaseRuntime(
                invalidProvider,
                new DatabaseExecutor(),
                new SchemaMigrationService(List.of(new InitialSchemaMigration())),
                new SQLiteGrowthToolRepository(invalidProvider),
                new SQLitePlacedBlockRepository(invalidProvider),
                Logger.getLogger("GrowthToolsDatabaseFailureTest"));

        try (runtime) {
            assertThrows(CompletionException.class, () -> runtime.start().join());
            assertFalse(runtime.isReady());
            assertFalse(invalidProvider.isAvailable());
            assertFalse(runtime.stats().join().connected());
        }
    }

    @Test
    void entersDegradedModeAfterARealSQLiteWriteLock() throws Exception {
        SQLitePlacedBlockRepository placedBlocks = new SQLitePlacedBlockRepository(provider);
        DatabaseRuntime runtime = new DatabaseRuntime(
                provider,
                new DatabaseExecutor(),
                migrations,
                new SQLiteGrowthToolRepository(provider),
                placedBlocks,
                Logger.getLogger("GrowthToolsDatabaseLockTest"));
        runtime.start().join();

        try (runtime;
                Connection lock = DriverManager.getConnection(
                        "jdbc:sqlite:" + provider.databasePath());
                Statement statement = lock.createStatement()) {
            lock.setAutoCommit(false);
            statement.executeUpdate("""
                    INSERT INTO audit_log(operation, details, created_at)
                    VALUES ('lock-test', 'uncommitted lock holder', 1)
                    """);

            PlacedBlockRecord record = new PlacedBlockRecord(
                    new PlacedBlockKey(UUID.randomUUID(), 1, 64, 1),
                    1_000,
                    UUID.randomUUID());
            assertThrows(CompletionException.class,
                    () -> runtime.run(() -> placedBlocks.upsert(record),
                            "locked placed-block write").join());
            assertFalse(runtime.isReady());
            assertThrows(CompletionException.class,
                    () -> runtime.run(() -> { }, "operation after degradation").join());
            lock.rollback();
        }
    }
}
