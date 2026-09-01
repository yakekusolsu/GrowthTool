package dev.yakekusolsu.growthtools.data;

import dev.yakekusolsu.growthtools.model.GrowthToolData;
import dev.yakekusolsu.growthtools.util.migration.MigrationStep;

/** Forward-only migration for validated portable GrowthTool data. */
public interface GrowthToolDataMigration extends MigrationStep<GrowthToolData> {}
