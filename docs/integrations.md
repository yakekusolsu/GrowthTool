# Optional integrations

`IntegrationManager` owns adapter initialization, shutdown, dynamic plugin enable/disable
refresh, and the `AVAILABLE`, `UNAVAILABLE`, `DISABLED`, and `ERROR` states. External API
classes live only below `integration/<plugin>` and their dependencies are `compileOnly`.

- PlaceholderAPI registers the `growthtools` expansion for main-hand tool fields: `level`,
  `exp`, `exp_current`, `exp_required`, `type`, `uuid`, `max_level`, `is_max_level`, and
  `ability_<namespace:id>_unlocked`.
- Vault exposes an internal economy abstraction for future costs; no gameplay charge exists.
- mcMMO marks synthetic additional blocks as unnatural unless extra rewards are enabled.
- Jobs cancels only matching `JobsPrePaymentEvent` during a scoped ability-origin break.
- WorldGuard checks `Flags.BLOCK_BREAK` before each synthetic break. Adapter errors deny that
  synthetic break; the player's original break is unaffected.
- Geyser is detected for health output. Floodgate supplies Bedrock UUID detection.

All adapters are optional. A `LinkageError` or runtime initialization failure marks only that
adapter `ERROR` and logs a warning. Normal cancellable `BlockBreakEvent` compatibility remains
the fallback when WorldGuard is absent or disabled.

Reported Paper 1.21.11 player QA passed WorldGuard denial for normal/Vein/Area breaks,
core held-tool PlaceholderAPI values, mcMMO normal/Vein reward isolation, Jobs
normal/Vein/Area reward isolation, and the documented Geyser/Floodgate Bedrock gameplay scope.
Vault with an economy provider, mcMMO Area rewards, and unlisted placeholder/configuration
variants remain untested or manual; see `compatibility.md` for exact status.
