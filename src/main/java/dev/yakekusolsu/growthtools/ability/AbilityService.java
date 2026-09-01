package dev.yakekusolsu.growthtools.ability;

import dev.yakekusolsu.growthtools.api.ability.AbilityDefinition;
import dev.yakekusolsu.growthtools.api.ability.AbilityId;
import dev.yakekusolsu.growthtools.api.ability.AbilityResult;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Central condition, cooldown, executor and result orchestration. */
public final class AbilityService {
    private final AbilityRegistry registry;
    private final CooldownService cooldowns;
    private final Map<AbilityId, AbilityExecutor> executors = new HashMap<>();
    private final Logger logger;

    public AbilityService(AbilityRegistry registry, CooldownService cooldowns) {
        this(registry, cooldowns, Logger.getLogger(AbilityService.class.getName()));
    }

    public AbilityService(AbilityRegistry registry, CooldownService cooldowns, Logger logger) {
        this.registry = registry;
        this.cooldowns = cooldowns;
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    public void registerExecutor(AbilityId id, AbilityExecutor executor) {
        Objects.requireNonNull(executor, "executor");
        if (executors.putIfAbsent(id, executor) != null) {
            throw new IllegalArgumentException("Duplicate executor for " + id);
        }
    }

    public void unregisterExecutor(AbilityId id) {
        executors.remove(id);
    }

    public AbilityExecution execute(AbilityId id, PaperAbilityExecutionContext context) {
        AbilityDefinition definition = registry.get(id).orElse(null);
        if (definition == null) {
            return new AbilityExecution(null,
                    AbilityResult.skipped(AbilityResult.Status.NOT_TRIGGERED, "not registered"));
        }
        try {
            return executeSafely(definition, context);
        } catch (RuntimeException | LinkageError exception) {
            logger.log(Level.SEVERE, "Ability " + id
                    + " failed in an isolated execution; remaining abilities will continue.",
                    exception);
            return new AbilityExecution(definition,
                    AbilityResult.skipped(AbilityResult.Status.ERROR,
                            "executor or condition failed; see server log"));
        }
    }

    private AbilityExecution executeSafely(
            AbilityDefinition definition, PaperAbilityExecutionContext context) {
        if (!definition.canActivate(context.domain())) {
            AbilityResult.Status status;
            if (!definition.enabled()) status = AbilityResult.Status.DISABLED;
            else if (context.domain().trigger() != definition.trigger())
                status = AbilityResult.Status.NOT_TRIGGERED;
            else if (context.domain().toolLevel() < definition.unlockLevel())
                status = AbilityResult.Status.LOCKED;
            else if (!definition.supportedToolTypes().contains(context.domain().toolType()))
                status = AbilityResult.Status.INCOMPATIBLE;
            else status = AbilityResult.Status.CONDITION_FAILED;
            return new AbilityExecution(definition, AbilityResult.skipped(status, "policy rejected"));
        }
        if (cooldowns.isOnCooldown(context.domain().toolId(), definition.id())) {
            return new AbilityExecution(definition,
                    AbilityResult.skipped(AbilityResult.Status.COOLDOWN, "cooldown active"));
        }
        AbilityExecutor executor = executors.get(definition.id());
        if (executor == null) {
            return new AbilityExecution(definition,
                    AbilityResult.skipped(AbilityResult.Status.NOT_TRIGGERED, "no executor"));
        }
        AbilityResult result = executor.execute(context, definition);
        if (result.status() == AbilityResult.Status.SUCCESS) {
            cooldowns.start(context.domain().toolId(), definition.id(), definition.cooldown());
        }
        return new AbilityExecution(definition, result);
    }

    public List<AbilityExecution> executeTrigger(PaperAbilityExecutionContext context,
            java.util.Set<AbilityId> excluded) {
        return registry.getAll().stream()
                .filter(definition -> definition.trigger() == context.domain().trigger())
                .filter(definition -> !excluded.contains(definition.id()))
                .map(definition -> execute(definition.id(), context))
                .toList();
    }

    public long remainingCooldownMillis(AbilityId id, java.util.UUID toolId) {
        return cooldowns.remainingMillis(toolId, id);
    }
}
