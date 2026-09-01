package dev.yakekusolsu.growthtools.storage.database;

import dev.yakekusolsu.growthtools.model.PlacedBlockKey;
import dev.yakekusolsu.growthtools.model.PlacedBlockRecord;
import dev.yakekusolsu.growthtools.storage.PlacedBlockRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class SQLitePlacedBlockRepository implements PlacedBlockRepository {
    private final DatabaseProvider provider;

    public SQLitePlacedBlockRepository(DatabaseProvider provider) {
        this.provider = provider;
    }

    @Override
    public Collection<PlacedBlockRecord> loadAll() throws SQLException {
        List<PlacedBlockRecord> records = new ArrayList<>();
        try (Connection connection = provider.openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT world_uuid, x, y, z, placed_at, player_uuid FROM placed_blocks");
                ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                PlacedBlockKey key = new PlacedBlockKey(
                        UUID.fromString(result.getString("world_uuid")),
                        result.getInt("x"), result.getInt("y"), result.getInt("z"));
                records.add(new PlacedBlockRecord(
                        key,
                        result.getLong("placed_at"),
                        UUID.fromString(result.getString("player_uuid"))));
            }
        }
        return records;
    }

    @Override
    public void upsert(PlacedBlockRecord block) throws SQLException {
        String sql = """
                INSERT INTO placed_blocks (
                    world_uuid, x, y, z, chunk_x, chunk_z, placed_at, player_uuid
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(world_uuid, x, y, z) DO UPDATE SET
                    placed_at = excluded.placed_at,
                    player_uuid = excluded.player_uuid
                """;
        try (Connection connection = provider.openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bind(statement, block);
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(PlacedBlockKey key) throws SQLException {
        try (Connection connection = provider.openConnection();
                PreparedStatement statement = connection.prepareStatement("""
                        DELETE FROM placed_blocks
                        WHERE world_uuid = ? AND x = ? AND y = ? AND z = ?
                        """)) {
            bindKey(statement, key);
            statement.executeUpdate();
        }
    }

    @Override
    public void move(PlacedBlockKey from, PlacedBlockRecord to) throws SQLException {
        try (Connection connection = provider.openConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement delete = connection.prepareStatement("""
                            DELETE FROM placed_blocks
                            WHERE world_uuid = ? AND x = ? AND y = ? AND z = ?
                            """);
                    PreparedStatement insert = connection.prepareStatement("""
                            INSERT OR REPLACE INTO placed_blocks (
                                world_uuid, x, y, z, chunk_x, chunk_z, placed_at, player_uuid
                            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                            """)) {
                bindKey(delete, from);
                delete.executeUpdate();
                bind(insert, to);
                insert.executeUpdate();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    @Override
    public long count() throws SQLException {
        try (Connection connection = provider.openConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT COUNT(*) FROM placed_blocks");
                ResultSet result = statement.executeQuery()) {
            return result.next() ? result.getLong(1) : 0L;
        }
    }

    private static void bind(PreparedStatement statement, PlacedBlockRecord block)
            throws SQLException {
        bindKey(statement, block.key());
        statement.setInt(5, block.key().chunkX());
        statement.setInt(6, block.key().chunkZ());
        statement.setLong(7, block.placedAt());
        statement.setString(8, block.playerId().toString());
    }

    private static void bindKey(PreparedStatement statement, PlacedBlockKey key)
            throws SQLException {
        statement.setString(1, key.worldId().toString());
        statement.setInt(2, key.x());
        statement.setInt(3, key.y());
        statement.setInt(4, key.z());
    }
}
