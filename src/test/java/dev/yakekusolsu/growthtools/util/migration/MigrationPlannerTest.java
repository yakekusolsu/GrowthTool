package dev.yakekusolsu.growthtools.util.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class MigrationPlannerTest {
    @Test
    void selectsAndAppliesContiguousConfigMigrationSteps() {
        MigrationPlanner<String> planner = new MigrationPlanner<>(List.of(
                step(1, 2, "-v2"),
                step(0, 1, "-v1")));

        assertEquals(2, planner.plan(0, 2).size());
        assertEquals("config-v1-v2", planner.migrate("config", 0, 2));
        assertEquals(0, planner.plan(2, 2).size());
    }

    @Test
    void refusesAConfigMigrationGap() {
        MigrationPlanner<String> planner = new MigrationPlanner<>(List.of(step(1, 2, "-v2")));
        assertThrows(IllegalStateException.class, () -> planner.plan(0, 2));
    }

    private static MigrationStep<String> step(int from, int to, String suffix) {
        return new MigrationStep<>() {
            @Override public int fromVersion() { return from; }
            @Override public int toVersion() { return to; }
            @Override public String migrate(String value) { return value + suffix; }
        };
    }
}
