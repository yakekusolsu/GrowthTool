package dev.yakekusolsu.growthtools.api.internal;

import dev.yakekusolsu.growthtools.ability.*;
import dev.yakekusolsu.growthtools.api.ability.*;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.plugin.Plugin;

public final class AbilityManagerImpl implements AbilityManager {
    private final ApiLifecycle lifecycle;
    private final AbilityRegistry registry;
    private final AbilityService service;
    private final Clock clock;
    private final Map<AbilityId, Registration> registrations = new ConcurrentHashMap<>();

    public AbilityManagerImpl(ApiLifecycle lifecycle, AbilityRegistry registry, AbilityService service) {
        this(lifecycle, registry, service, Clock.systemUTC());
    }
    AbilityManagerImpl(ApiLifecycle lifecycle, AbilityRegistry registry,
            AbilityService service, Clock clock) {
        this.lifecycle = lifecycle; this.registry = registry; this.service = service; this.clock = clock;
    }

    public void trackBuiltin(Plugin owner, AbilityId id) {
        registrations.put(id, new Registration(id, owner, Instant.now(clock), null));
    }

    @Override public AbilityRegistration register(Plugin owner, AbilityDefinition definition,
            AddonAbilityExecutor executor) {
        lifecycle.ensureActive(); MainThreadGuard.check();
        java.util.Objects.requireNonNull(owner, "owner");
        java.util.Objects.requireNonNull(definition, "definition");
        java.util.Objects.requireNonNull(executor, "executor");
        if (!owner.isEnabled()) throw new IllegalStateException("Addon plugin is not enabled");
        verifyNamespace(owner, definition.id());
        registry.register(definition);
        try {
            service.registerExecutor(definition.id(), (context, registeredDefinition) ->
                    executor.execute(new AddonAbilityContext(context.domain(), context.player(),
                            context.tool(), context.origin()), registeredDefinition));
        } catch (RuntimeException exception) {
            registry.unregister(definition.id());
            throw exception;
        }
        Registration registration = new Registration(
                definition.id(), owner, Instant.now(clock), executor);
        registrations.put(definition.id(), registration);
        return registration;
    }

    @Override public Optional<AbilityRegistrationInfo> registration(AbilityId id) {
        lifecycle.ensureActive();
        return Optional.ofNullable(registrations.get(id)).map(Registration::info);
    }

    @Override public Collection<AbilityRegistrationInfo> registrations() {
        lifecycle.ensureActive();
        return registrations.values().stream().map(Registration::info)
                .sorted(Comparator.comparing(AbilityRegistrationInfo::id)).toList();
    }

    public void unregisterOwnedBy(Plugin plugin) {
        MainThreadGuard.check();
        registrations.values().stream().filter(value -> value.owner == plugin)
                .toList().forEach(Registration::unregister);
    }

    private static void verifyNamespace(Plugin owner, AbilityId id) {
        String namespace = owner.getName().toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "");
        if (!id.namespace().equals(namespace)) {
            throw new SecurityException("Plugin " + owner.getName()
                    + " does not own namespace " + id.namespace());
        }
    }

    private final class Registration implements AbilityRegistration {
        private final AbilityId id;
        private final Plugin owner;
        private final Instant registeredAt;
        @SuppressWarnings("unused") private AddonAbilityExecutor executorReference;
        private volatile boolean registered = true;
        private Registration(AbilityId id, Plugin owner, Instant registeredAt,
                AddonAbilityExecutor executorReference) {
            this.id = id; this.owner = owner; this.registeredAt = registeredAt;
            this.executorReference = executorReference;
        }
        @Override public AbilityId id() { lifecycle.ensureActive(); return id; }
        @Override public Plugin owner() { lifecycle.ensureActive(); return owner; }
        @Override public boolean isRegistered() { lifecycle.ensureActive(); return registered; }
        @Override public boolean unregister() {
            lifecycle.ensureActive(); MainThreadGuard.check();
            if (!registered) return false;
            registered = false;
            registry.unregister(id); service.unregisterExecutor(id); executorReference = null;
            return true;
        }
        private AbilityRegistrationInfo info() {
            return new AbilityRegistrationInfo(id, owner.getName(), registeredAt,
                    registered ? AbilityRegistrationStatus.REGISTERED
                            : AbilityRegistrationStatus.UNREGISTERED);
        }
    }
}
