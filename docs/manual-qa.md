# Manual QA profile — 0.7.0-alpha.1

Record exact versions and preserve sanitized evidence. `[x]` means only the scenario stated on that line was executed; unchecked lines remain `MANUAL REQUIRED` or `NOT TESTED` and are not inferred failures.

## Environment and smoke

- [x] Java 21.0.11; Paper 1.21.10 build 130; GrowthTools 0.7.0-alpha.1 server lifecycle profile
- [x] Paper 1.21.11 real human-player session: normal startup and core gameplay
- [x] Fresh enable, config validation, SQLite schema creation, four built-ins, and no startup exception
- [x] `/gt doctor`, `/gt give`, `/gt inspect`; held-item lore, UUID, level, and EXP display
- [x] Graceful stop, restart, database reopen, and graceful second stop in the recorded server profile
- [x] Diagnostic export inspected for IP/chat/token/secret/password/credential/session/player fields

## Core tools, EXP, persistence, and commands

- [x] Give and inspect a GrowthTool; validate lore, UUID, level, EXP, and registry-backed duplicate states
- [x] Survival Block EXP with representative pickaxe, axe, and shovel materials
- [x] Player-placed ore grants no Growth EXP before and after server restart
- [x] Duplicate UUID detection is non-destructive; explicit regenerate ID yields new `ACTIVE` and old `REPLACED`
- [ ] Give all six types and exercise every inventory movement/restart path
- [ ] EXP multi-level, maximum, overflow input, full lore/event ordering, and external API EXP
- [ ] Every command's invalid arguments, permission denial, and tab completion
- [ ] Conservative `/gt repair` and every malformed PDC refusal case

## Block EXP

- [x] STONE; coal/copper/iron/redstone/lapis/gold/diamond/emerald and deepslate ores with Growth Pickaxe
- [x] Log family with Growth Axe; DIRT, SAND, and sampled shovel blocks with Growth Shovel
- [x] Player-placed ore and restart-persistent placed-block rejection
- [ ] Full wrong-tool matrix, prismarine/brick/fallback groups, mature/immature crops and leaves with hoe
- [ ] Creative enabled/disabled and externally cancelled break matrix

## Vein Miner

- [x] Sneak activation and repeated player BlockBreak while continuing to hold sneak
- [x] Additional breaking, Block EXP interaction, and no recursive trigger
- [x] WorldGuard deny blocks additional breaking
- [ ] Exact traversal bound, diagonal toggle, player-placed target, and unloaded chunk edge
- [ ] Durability/Unbreaking distribution and mid-operation tool break
- [ ] Optional non-zero cooldown and `require-sneak: false` manual paths

## Area Break

- [x] 3×3 floor, wall, and ceiling planes
- [x] Repeated activation while continuing to hold sneak
- [x] Configured ore prefers Vein Miner; ordinary compatible target prefers Area Break; no recursion
- [x] WorldGuard deny blocks additional breaking
- [ ] Radius hard-limit, placed/unbreakable target, and unloaded chunk edge
- [ ] Durability/Unbreaking and mid-operation tool break
- [ ] Optional non-zero cooldown and `require-sneak: false` manual paths

## Auto Smelt

- [x] Normal Auto Smelt and Vein/Area additional-block composition
- [x] Mixed Area targets transform only smeltable drops
- [x] Fortune-adjusted quantity is preserved; no raw+smelted double drop
- [x] Default Silk Touch policy leaves ore-block drops unsmelted
- [ ] Every furnace recipe, no-recipe edge, event-disabled drops, and abnormal large stack bounds

## Experience Boost

- [ ] Normal/fractional EXP, multi-level, maximum, large input, and external API source

## Fishing and bow

- [x] Fishing cast gives no EXP; completed catch gives EXP
- [x] Bow mob hit gives EXP, miss gives none, and post-shot item switch credits the original Growth Bow
- [ ] Fishing entity catch, cancellation, rod switch/removal, and duplicate processing
- [ ] Bow zero/cancelled damage, bow removal, projectile duplicate, and all target types

## External integrations

- [x] WorldGuard: real deny region blocks normal, Vein additional, and Area additional breaks
- [x] mcMMO: normal and Vein paths do not amplify additional-block rewards unexpectedly
- [x] Jobs Reborn: normal, Vein, and Area paths respect `ability-extra-block-rewards: false`
- [x] PlaceholderAPI: held-tool level, EXP, type, UUID, maximum level, and max-level-state values expand
- [x] Geyser/Floodgate: real Bedrock client exercised GrowthTool, Block EXP, Vein, Area, fishing, and bow
- [ ] PlaceholderAPI no-tool and every ability placeholder
- [ ] mcMMO Area Break reward path
- [ ] Vault with an economy provider (`NOT TESTED`; no-provider isolation passed)

## Addon API and hostile input

- [x] Real Paper API acquisition, ability registration/info, owner-disable auto-unregister, and idempotent close
- [x] Automated runtime-exception isolation and continued dispatch
- [x] Automated namespace ownership, duplicate ID, reserved namespace, reload preservation/cleanup, and main-thread contract
- [ ] Real-player addon tool lookup, EXP grant, execution, public event, and reload-equivalent
- [ ] Real Paper player-triggered broken-addon executor with multiple addons

## SQLite and failures

- [x] Creation, schema/stats, diagnostics, clean shutdown, and reopen
- [x] Persistent placed-block coordinate survives restart and continues denying Growth EXP
- [x] Automated invalid path and real SQLite write lock enter degraded mode safely
- [ ] OS-level read-only database (`NOT TESTED`)

## Configuration and malformed state

- [x] Automated defaults, config-version handling, invalid/fallback validation
- [ ] Real Paper invalid Material, negative EXP/cooldown, excessive radius, invalid multiplier/path, and safe reload
- [ ] Malformed PDC UUID/type/version/negative EXP refusal without rewrite

## Release interpretation

The reported Paper 1.21.11 core/player, protection, data-integrity, reward-isolation, and Bedrock checks remove the former blanket player-QA blocker. Unchecked validation gaps remain documented and must not be described as tested; they are evaluated separately for alpha severity in `release-checklist.md` and `known-issues.md`.
