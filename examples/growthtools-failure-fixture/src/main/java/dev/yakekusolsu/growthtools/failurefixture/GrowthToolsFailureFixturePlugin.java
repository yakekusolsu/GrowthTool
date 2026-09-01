package dev.yakekusolsu.growthtools.failurefixture;

import dev.yakekusolsu.growthtools.api.GrowthToolsProvider;
import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.api.ability.AbilityRegistration;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;

/** Disposable real-Paper fixture proving that one failing addon does not stop another. */
public final class GrowthToolsFailureFixturePlugin extends JavaPlugin {
    private final List<AbilityRegistration> registrations = new ArrayList<>();

    @Override
    public void onEnable() {
        var abilities = GrowthToolsProvider.get().abilities();
        registrations.add(abilities.register(this, definition("broken"), (context, ignored) -> {
            throw new RuntimeException("Intentional GrowthTools failure-isolation QA exception");
        }));
        registrations.add(abilities.register(this, definition("survivor"), (context, ignored) -> {
            getLogger().info("Survivor ability executed after the intentional failure.");
            context.player().sendMessage("GrowthTools failure fixture survivor executed.");
            return AbilityResult.success(0, 0);
        }));
        getLogger().info("Registered intentional failure and survivor abilities.");
    }

    private static AbilityDefinition definition(String key) {
        return new AbilityDefinition(
                new AbilityId("growthtoolsfailurefixture", key),
                "Failure fixture " + key,
                "Disposable Phase 7.5 failure-isolation fixture.",
                AbilityTrigger.BLOCK_BREAK,
                true,
                1,
                Duration.ZERO,
                Set.of(GrowthToolType.PICKAXE),
                List.of(),
                Map.of());
    }

    @Override
    public void onDisable() {
        long removed = registrations.stream().filter(AbilityRegistration::unregister).count();
        getLogger().info("Explicitly unregistered " + removed
                + " fixture abilities; automatic cleanup may already have removed them.");
        registrations.clear();
    }
}
