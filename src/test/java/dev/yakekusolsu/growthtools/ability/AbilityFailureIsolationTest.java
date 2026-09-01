package dev.yakekusolsu.growthtools.ability;

import static org.junit.jupiter.api.Assertions.*;

import dev.yakekusolsu.growthtools.api.ability.*;
import dev.yakekusolsu.growthtools.model.GrowthToolType;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

class AbilityFailureIsolationTest {
    @Test void failingAddonDoesNotPreventLaterAbilityAndIsLogged() {
        AbilityRegistry registry = new AbilityRegistry();
        AbilityDefinition broken = definition("test:broken");
        AbilityDefinition healthy = definition("test:healthy");
        registry.register(broken);
        registry.register(healthy);
        AtomicBoolean logged = new AtomicBoolean();
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
            @Override public void publish(LogRecord record) { logged.set(true); }
            @Override public void flush() { }
            @Override public void close() { }
        });
        AbilityService service = new AbilityService(registry, new CooldownService(), logger);
        service.registerExecutor(broken.id(), (context, definition) -> {
            throw new IllegalStateException("intentional test failure");
        });
        service.registerExecutor(healthy.id(), (context, definition) ->
                AbilityResult.success(1, 0));

        var context = new PaperAbilityExecutionContext(
                new AbilityContext(UUID.randomUUID(), GrowthToolType.PICKAXE, 1,
                        AbilityTrigger.BLOCK_BREAK, Map.of()),
                proxy(Player.class), new ItemStack(Material.DIAMOND_PICKAXE), proxy(Block.class));
        List<AbilityExecution> results = service.executeTrigger(context, Set.of());

        assertEquals(AbilityResult.Status.ERROR, results.get(0).result().status());
        assertEquals(AbilityResult.Status.SUCCESS, results.get(1).result().status());
        assertTrue(logged.get());
    }

    private static AbilityDefinition definition(String id) {
        return new AbilityDefinition(AbilityId.parse(id), id, "Failure isolation test",
                AbilityTrigger.BLOCK_BREAK, true, 1, Duration.ZERO,
                Set.of(GrowthToolType.PICKAXE), List.of(), Map.of());
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (instance, method, arguments) -> {
                    Class<?> result = method.getReturnType();
                    if (!result.isPrimitive()) return null;
                    if (result == boolean.class) return false;
                    if (result == char.class) return '\0';
                    return 0;
                });
    }
}
