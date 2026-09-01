package dev.yakekusolsu.growthtools.util.migration;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Selects and applies a contiguous migration chain without skipping versions. */
public final class MigrationPlanner<T> {
    private final List<? extends MigrationStep<T>> steps;

    public MigrationPlanner(List<? extends MigrationStep<T>> steps) {
        this.steps = steps.stream()
                .sorted(Comparator.comparingInt(MigrationStep::fromVersion))
                .toList();
    }

    public List<? extends MigrationStep<T>> plan(int fromVersion, int targetVersion) {
        if (fromVersion < 0 || targetVersion < fromVersion) {
            throw new IllegalArgumentException("Invalid migration version range");
        }
        List<MigrationStep<T>> plan = new ArrayList<>();
        int version = fromVersion;
        while (version < targetVersion) {
            int current = version;
            MigrationStep<T> step = steps.stream()
                    .filter(candidate -> candidate.fromVersion() == current)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "No migration from version " + current));
            if (step.toVersion() <= version || step.toVersion() > targetVersion) {
                throw new IllegalStateException("Invalid migration step " + version + " -> "
                        + step.toVersion());
            }
            plan.add(step);
            version = step.toVersion();
        }
        return List.copyOf(plan);
    }

    public T migrate(T value, int fromVersion, int targetVersion) {
        T migrated = value;
        for (MigrationStep<T> step : plan(fromVersion, targetVersion)) {
            migrated = step.migrate(migrated);
        }
        return migrated;
    }
}
