package dev.yakekusolsu.growthtools.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GrowthToolDataMigrationServiceTest {
    @Test
    void selectsAndAppliesPdcMigration() {
        GrowthToolDataMigration migration = new GrowthToolDataMigration() {
            @Override public int fromVersion() { return 1; }
            @Override public int toVersion() { return 2; }
            @Override public GrowthToolData migrate(GrowthToolData data) {
                return new GrowthToolData(
                        data.toolId(), data.type(), data.level(), data.experience(),
                        data.createdAt(), 2);
            }
        };
        GrowthToolDataMigrationService service =
                new GrowthToolDataMigrationService(List.of(migration), 2);
        GrowthToolData original = data();

        assertEquals(1, service.select(1).size());
        assertEquals(2, service.migrate(original).dataVersion());
    }

    @Test
    void refusesMissingPdcMigrationPath() {
        GrowthToolDataMigrationService service =
                new GrowthToolDataMigrationService(List.of(), 2);
        assertThrows(IllegalStateException.class, () -> service.select(1));
    }

    private static GrowthToolData data() {
        return new GrowthToolData(
                UUID.randomUUID(), GrowthToolType.AXE, 1, 0, 1_700_000_000_000L, 1);
    }
}
