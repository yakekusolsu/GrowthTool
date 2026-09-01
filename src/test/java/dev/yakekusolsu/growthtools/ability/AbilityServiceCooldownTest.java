package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.yakekusolsu.growthtools.api.ability.AbilityContext;
import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import dev.yakekusolsu.growthtools.api.ability.AbilityTrigger;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class AbilityServiceCooldownTest {
    @Test
    void zeroCooldownAllowsConsecutiveBlockBreakExecutions() {
        TestHarness harness = new TestHarness(BuiltinAbilities.VEIN_MINER, Duration.ZERO);

        assertEquals(AbilityResult.Status.SUCCESS, harness.execute().result().status());
        assertEquals(AbilityResult.Status.SUCCESS, harness.execute().result().status());
    }

    @Test
    void oneSecondCooldownRejectsImmediateSecondBlockBreakExecution() {
        TestHarness harness = new TestHarness(
                BuiltinAbilities.VEIN_MINER, Duration.ofSeconds(1));

        assertEquals(AbilityResult.Status.SUCCESS, harness.execute().result().status());
        assertEquals(AbilityResult.Status.COOLDOWN, harness.execute().result().status());
    }

    @Test
    void areaBreakZeroCooldownAllowsConsecutiveBlockBreakExecutions() {
        TestHarness harness = new TestHarness(BuiltinAbilities.AREA_BREAK, Duration.ZERO);

        assertEquals(AbilityResult.Status.SUCCESS, harness.execute().result().status());
        assertEquals(AbilityResult.Status.SUCCESS, harness.execute().result().status());
    }

    @Test
    void configuredAreaBreakCooldownRejectsImmediateSecondExecution() {
        TestHarness harness = new TestHarness(
                BuiltinAbilities.AREA_BREAK, Duration.ofSeconds(3));

        assertEquals(AbilityResult.Status.SUCCESS, harness.execute().result().status());
        assertEquals(AbilityResult.Status.COOLDOWN, harness.execute().result().status());
    }

    private static final class TestHarness {
        private final AbilityService service;
        private final PaperAbilityExecutionContext context;
        private final AbilityId abilityId;

        private TestHarness(AbilityId abilityId, Duration cooldown) {
            AbilityRegistry registry = new AbilityRegistry();
            registry.register(new AbilityDefinition(
                    abilityId,
                    "Mining ability",
                    "Test definition",
                    AbilityTrigger.BLOCK_BREAK,
                    true,
                    1,
                    cooldown,
                    Set.of(GrowthToolType.PICKAXE),
                    List.of(),
                    Map.of()));
            service = new AbilityService(registry, new CooldownService(new MutableClock()));
            service.registerExecutor(abilityId, (executionContext, definition) ->
                    AbilityResult.success(1, 0));
            this.abilityId = abilityId;
            UUID toolId = UUID.randomUUID();
            context = new PaperAbilityExecutionContext(
                    new AbilityContext(toolId, GrowthToolType.PICKAXE, 25,
                            AbilityTrigger.BLOCK_BREAK, Map.of("sneaking", "true")),
                    proxy(Player.class),
                    new ItemStack(Material.DIAMOND_PICKAXE),
                    proxy(Block.class));
        }

        private AbilityExecution execute() {
            return service.execute(abilityId, context);
        }
    }

    private static <T> T proxy(Class<T> type) {
        Object proxy = Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[] {type},
                (ignored, method, arguments) -> defaultValue(method.getReturnType()));
        return type.cast(proxy);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        return 0;
    }

    private static final class MutableClock extends Clock {
        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis());
        }

        @Override
        public long millis() {
            return 1_000L;
        }
    }
}
