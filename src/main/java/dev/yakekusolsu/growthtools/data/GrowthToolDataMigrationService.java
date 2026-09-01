package dev.yakekusolsu.growthtools.data;

import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.storage.GrowthToolKeys;
import dev.yakekusolsu.growthtools.util.migration.MigrationPlanner;
import java.util.List;

/** PDC migration framework. Version 1 remains current until a real format change exists. */
public final class GrowthToolDataMigrationService {
    private final MigrationPlanner<GrowthToolData> planner;
    private final int targetVersion;

    public GrowthToolDataMigrationService(List<? extends GrowthToolDataMigration> migrations) {
        this(migrations, GrowthToolKeys.CURRENT_DATA_VERSION);
    }

    GrowthToolDataMigrationService(
            List<? extends GrowthToolDataMigration> migrations, int targetVersion) {
        planner = new MigrationPlanner<>(migrations);
        this.targetVersion = targetVersion;
    }

    public GrowthToolData migrate(GrowthToolData data) {
        return planner.migrate(
                data, data.dataVersion(), targetVersion);
    }

    public List<? extends MigrationStepView> select(int fromVersion) {
        return planner.plan(fromVersion, targetVersion).stream()
                .map(step -> new MigrationStepView(step.fromVersion(), step.toVersion()))
                .toList();
    }

    public record MigrationStepView(int fromVersion, int toVersion) {}
}
