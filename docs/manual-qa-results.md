# Alpha QA results — 0.7.0-alpha.1

Results describe only scenarios actually executed through 2026-09-01. `PASS` is limited to the Notes column and does not imply that every row in `player-qa.md` was executed. The Paper 1.21.11 player results below were reported from a real human-player QA session; the exact Paper build number was not separately recorded in that report.

| Test | Environment | Result | Notes |
| --- | --- | --- | --- |
| Release build and automated tests | Java 21, Gradle 8.14.3 | PASS | Unit, integration, API baseline, Javadocs, artifacts, and JAR boundary audits pass |
| Paper startup/shutdown | Paper 1.21.4 build 232 | PASS | GrowthTools enable, config/SQLite initialization, console commands, graceful shutdown |
| Paper startup/restart/shutdown | Paper 1.21.10 build 130 | PASS | Core lifecycle, diagnostics, SQLite reopen, graceful shutdown |
| Paper player startup/core | Paper 1.21.11 + human player | PASS | Normal enable; `/gt doctor`, `/gt give`, `/gt inspect`; held-tool lore, UUID, level, and EXP display |
| Survival Block EXP — pickaxe | Paper 1.21.11 + human player | PASS | STONE; coal, copper, iron, redstone, lapis, gold, diamond, emerald and deepslate ores; additional standard pickaxe targets sampled |
| Survival Block EXP — axe/shovel | Paper 1.21.11 + human player | PASS | Log family with axe; DIRT, SAND, and sampled shovel targets with shovel |
| Extended Block EXP matrix | Paper + human player | MANUAL REQUIRED | Hoe/crop maturity, every fallback category, wrong-tool matrix, cancellation, and Creative policy were not all reported |
| Persistent placed-block protection | Paper 1.21.11 + human player | PASS | Player-placed ore granted no Growth EXP before and after a full server restart |
| Vein Miner core | Paper 1.21.11 + human player | PASS | Sneak activation, held-sneak consecutive player breaks, additional breaks, no recursive trigger, and Block EXP interaction |
| Vein Miner extended safety | Paper + human player | MANUAL REQUIRED | Exact max-block bound, unloaded chunk edge, Unbreaking distribution, and mid-operation tool break were not reported |
| Area Break core | Paper 1.21.11 + human player | PASS | 3×3, held-sneak consecutive activation, floor/wall/ceiling planes, Vein priority, and no recursion |
| Area Break extended safety | Paper + human player | MANUAL REQUIRED | Radius hard-limit, unloaded edge, Unbreaking, and mid-operation tool break were not reported |
| Auto Smelt composition | Paper 1.21.11 + human player | PASS | Normal use plus Vein and Area additional blocks, mixed targets, and no raw+smelted double drop |
| Fortune / Silk Touch | Paper 1.21.11 + human player | PASS | Fortune-adjusted quantity retained after smelting; Silk Touch kept the ore block and bypassed Auto Smelt |
| Experience Boost | Paper + human player | MANUAL REQUIRED | Fractional floor, multi-level, maximum, overflow-sized, and external API inputs were not reported |
| Fishing core | Paper 1.21.11 + human player | PASS | Cast alone gave no EXP; completed catch gave Growth EXP |
| Fishing extended attribution | Paper + human player | MANUAL REQUIRED | Entity catch, cancellation, rod removal/switch, and duplicate-processing matrix were not reported |
| Bow core and attribution | Paper 1.21.11 + human player | PASS | Mob hit gave EXP, miss gave none, and post-shot held-item switch still credited the original Growth Bow |
| Bow extended safety | Paper + human player | MANUAL REQUIRED | Zero/cancelled damage, removed bow, duplicate projectile handling, and all target types were not reported |
| Duplicate UUID / regenerate ID | Paper 1.21.11 + human player | PASS | Duplicate detected as `DUPLICATE`, neither item auto-deleted; one item regenerated to a new `ACTIVE` UUID and old UUID became `REPLACED` |
| Conservative repair/corrupt PDC refusal | Disposable QA items + human player | MANUAL REQUIRED | Lore/cache/row repair and malformed UUID/type/version refusal were not reported in this session |
| PlaceholderAPI lifecycle | Paper 1.21.10, PAPI 2.12.3 | PASS | Plugin enabled, expansion registered, adapter `AVAILABLE` |
| PlaceholderAPI held-tool values | Paper 1.21.11 + human player | PASS | Core level, EXP, type, UUID, max-level, and max-level-state placeholders expanded |
| PlaceholderAPI extended values | Paper + human player | MANUAL REQUIRED | No-tool values and every ability placeholder were not explicitly reported |
| Vault without provider | Paper 1.21.10, Vault 1.7.3-b131 | PASS | Missing-provider integration error was isolated; GrowthTools stayed enabled |
| Vault with provider | Paper + economy provider | NOT TESTED | No provider-present runtime QA was reported |
| mcMMO lifecycle | Paper 1.21.10, mcMMO 2.3.000 | PASS | Plugin and adapter loaded/stopped cleanly |
| mcMMO player rewards | Paper 1.21.11 + human player | PASS | Normal block and Vein Miner checked; additional blocks did not cause unintended reward amplification |
| mcMMO Area reward path | Paper + human player | MANUAL REQUIRED | Area Break reward behavior was not explicitly reported |
| Jobs lifecycle | Paper 1.21.10, Jobs artifact 5.2.6.2 / metadata 5.2.6.1, CMILib 1.5.9.7 | PASS | Plugin and adapter loaded/stopped cleanly |
| Jobs player rewards | Paper 1.21.11 + human player | PASS | Normal, Vein, and Area checked with `ability-extra-block-rewards: false`; additional-block rewards did not multiply |
| WorldGuard lifecycle | Paper 1.21.10, WE 7.3.9, WG 7.0.13 | PASS | Plugins and adapter loaded/stopped cleanly |
| WorldGuard protection | Paper 1.21.11 deny region + human player | PASS | Normal, Vein additional, and Area additional breaks could not bypass `block-break deny` |
| Geyser/Floodgate lifecycle | Geyser 2.11.2-b1233, Floodgate 2.2.5-b140 | PASS | Both adapters `AVAILABLE`, UDP listener started, clean stop |
| Bedrock gameplay | Real Bedrock client through Geyser/Floodgate | PASS | GrowthTool use, Block EXP, Vein Miner, Area Break, fishing, and bow were exercised |
| Example addon lifecycle | Paper 1.21.10 | PASS | API acquisition, registration/inspection, automatic owner cleanup |
| Addon executor isolation | Automated tests | PASS | Intentional executor exception returns `ERROR` and a later ability continues |
| Addon executor isolation | Paper + human player | MANUAL REQUIRED | Player-triggered failure fixture execution was not reported |
| SQLite creation/reopen | Paper 1.21.4/1.21.10 | PASS | Schema/stats, diagnostics, clean shutdown, and reopen |
| SQLite invalid path / write lock | Automated tests | PASS | Failures enter degraded mode without crashing; later database work is rejected |
| SQLite read-only file | Windows OS-level permissions | NOT TESTED | Not safely reproduced |
| Git tracking dry run | Repository root | PASS | Rechecked during final alpha audit; exclusions and addable release files are recorded in the completion report |

No reported GrowthTools player scenario failed. Remaining rows are explicit validation gaps, not inferred failures.
