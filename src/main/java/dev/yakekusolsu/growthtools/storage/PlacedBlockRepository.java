package dev.yakekusolsu.growthtools.storage;

import dev.yakekusolsu.growthtools.model.PlacedBlockKey;
import dev.yakekusolsu.growthtools.model.PlacedBlockRecord;
import java.sql.SQLException;
import java.util.Collection;

/** Storage abstraction for restart-persistent placed-block tracking. */
public interface PlacedBlockRepository {
    Collection<PlacedBlockRecord> loadAll() throws SQLException;

    void upsert(PlacedBlockRecord block) throws SQLException;

    void delete(PlacedBlockKey key) throws SQLException;

    void move(PlacedBlockKey from, PlacedBlockRecord to) throws SQLException;

    long count() throws SQLException;
}
