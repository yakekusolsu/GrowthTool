package dev.yakekusolsu.growthtools.api.internal;

import dev.yakekusolsu.growthtools.api.experience.*;
import dev.yakekusolsu.growthtools.model.ExperienceSource;
import dev.yakekusolsu.growthtools.service.ExperienceService;
import dev.yakekusolsu.growthtools.service.GrowthToolItemService;
import dev.yakekusolsu.growthtools.service.GrowthToolUpdateService;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ExperienceManagerImpl implements ExperienceManager {
    private final ApiLifecycle lifecycle;
    private final GrowthToolItemService items;
    private final GrowthToolUpdateService updates;
    private final ExperienceService experience;
    public ExperienceManagerImpl(ApiLifecycle lifecycle, GrowthToolItemService items,
            GrowthToolUpdateService updates, ExperienceService experience) {
        this.lifecycle = lifecycle; this.items = items; this.updates = updates;
        this.experience = experience;
    }
    @Override public Optional<ExperienceChangeResult> addExperience(Player player, ItemStack tool,
            long amount, ExperienceSourceId source) {
        lifecycle.ensureActive(); MainThreadGuard.check();
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        java.util.Objects.requireNonNull(source, "source");
        return updates.addExperience(player, tool, ExperienceSource.API, source, amount).map(result ->
                new ExperienceChangeResult(
                        ToolSnapshotMapper.map(result.oldData(), experience.maximumLevel()),
                        ToolSnapshotMapper.map(result.newData(), experience.maximumLevel()),
                        amount, result.experienceAdded()));
    }
}
