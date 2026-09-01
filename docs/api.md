# Experimental developer API v1

The Phase 6 API is a pre-release boundary. Compile against
`build/libs/GrowthTools-api-0.7.0-alpha.1.jar` with `compileOnly`, add
`depend: [GrowthTools]` to the addon `plugin.yml`, and never bundle the API into the addon.

## Getting the API

```java
if (!GrowthToolsProvider.isAvailable()) return;
GrowthToolsAPI api = GrowthToolsProvider.get();
if (api.version().major() != 1) throw new IllegalStateException("Unsupported API");
```

The provider is registered during GrowthTools enable and cleared during disable. Every
manager facade checks its lifecycle; using a retained API after disable throws
`IllegalStateException` instead of touching closed services.

## Tool lookup and EXP

```java
ItemStack item = player.getInventory().getItemInMainHand();
api.tools().getTool(item).ifPresent(tool ->
        logger.info(tool.toolId() + " level=" + tool.level()));

api.experience().addExperience(player, item, 25,
        new ExperienceSourceId("myaddon", "quest_reward"));
```

`GrowthToolSnapshot` is immutable. EXP mutation follows the existing modifier, cap, PDC,
lore, registry, event, unlock, and notification pipeline. There is intentionally no public
`setLevel` or `setExperience`.

## Registering an ability

```java
AbilityDefinition definition = new AbilityDefinition(
        new AbilityId("myaddon", "sample"), "Sample", "Example ability",
        AbilityTrigger.BLOCK_BREAK, true, 10, Duration.ZERO,
        Set.of(GrowthToolType.PICKAXE), List.of(), Map.of());
AbilityRegistration handle = api.abilities().register(this, definition,
        (context, registered) -> AbilityResult.success(0, 0));
```

The namespace must equal the owning plugin name after lowercase normalization and removal
of characters outside `[a-z0-9._-]`. An addon cannot register `growthtools:*` or another
plugin's namespace. IDs, owner name, registration time, and status are tracked. The handle's
`unregister()` is idempotent. GrowthTools also unregisters every owned definition/executor
on the owner's `PluginDisableEvent`; `/gt reload` preserves addon registrations.

Phase 6 automatically dispatches registered `BLOCK_BREAK` abilities. Other trigger values
are API/domain preparation and are not a promise of automatic gameplay dispatch yet.

## Events

Listen normally with Bukkit for `GrowthToolExperienceGainEvent`, `GrowthToolLevelUpEvent`,
`GrowthToolAbilityActivateEvent`, and `GrowthToolAbilityUnlockEvent` in `api.event`.
Event getters expose API records, Bukkit types, UUID, primitives, and `GrowthToolType`; they
do not expose implementation services or mutable internal domain data. ItemStack remains a
live Bukkit object and must not be retained or mutated asynchronously.

## Thread contract

- `GrowthToolsProvider.isAvailable()`, `GrowthToolsProvider.get()`, `api.version()`, and
  manager metadata reads are async-safe.
- Tool lookup is **MAIN THREAD ONLY** because `ItemStack` is mutable Bukkit state.
- EXP mutation, ability registration/unregister, and addon executors are **MAIN THREAD ONLY**.
- Thread violations and stale lifecycle access throw `IllegalStateException`.
- No public method returns `null`; absence uses `Optional`. Invalid arguments throw
  `IllegalArgumentException`, and namespace violations throw `SecurityException`.

See [API versioning](api-versioning.md) and the
[example addon](../examples/growthtools-example-addon/README.md).
