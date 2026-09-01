# Ability framework and public API

Phase 5 introduced the internal framework. Phase 6 exposes its experimental addon boundary
through `GrowthToolsAPI.abilities()`. The plugin remains 0.x, so this is not yet a 1.0 binary
compatibility promise; see [the API guide](api.md) and [versioning policy](api-versioning.md).

```text
Gameplay event
  -> AbilityTriggerResolver
  -> AbilityContext (Paper-independent)
  -> AbilityService
  -> AbilityRegistry / AbilityDefinition / AND conditions / CooldownService
  -> AbilityExecutor (Paper adapter context)
  -> AbilityResult
  -> success-only Bukkit event
```

`AbilityId` is a validated `namespace:key` value rather than a closed enum. `AbilityDefinition` is immutable and describes the trigger, supported types, unlock level, enabled state, cooldown, AND-composed conditions, and string settings. Duplicate registration is rejected without replacement.

`AbilityContext` carries only UUID, tool type, level, trigger, and immutable attributes. Public
executors receive `AddonAbilityContext`; internal Paper contexts and executor services are not
part of the API artifact. The service owns policy, cooldown, dispatch, and result status.

`AbilityManager.register(plugin, definition, executor)` returns an idempotent handle and tracks
owner/time/status. Namespace ownership is enforced, addon disable automatically releases its
definition and executor, and GrowthTools reload preserves addon registrations. Activation fires
only for `SUCCESS`; unlock fires once per crossed definition threshold. Phase 6 automatically
dispatches addon `BLOCK_BREAK` definitions; other trigger dispatch remains future work.
