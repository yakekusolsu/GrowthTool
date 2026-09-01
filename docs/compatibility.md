# Compatibility matrix

Status definitions: `PLAYER QA TESTED` means a human exercised the stated gameplay on the named runtime. `TESTED` means the runtime was launched for only the stated non-player/server scenario. `COMPILE VERIFIED` means only its public API compiled. `EXPECTED COMPATIBLE` is an informed expectation, not runtime evidence. `MANUAL REQUIRED` and `NOT TESTED` identify remaining gaps.

## Paper and Java

| Component | Version | Status | Evidence / scope |
| --- | --- | --- | --- |
| Java | Eclipse Temurin 21.0.11 | TESTED | Build, tests, and both Paper profiles |
| Paper | Minecraft 1.21.4, build 232 (`12d8fe0`) | TESTED | Fresh startup, GrowthTools enable, config/SQLite initialization, console commands, graceful shutdown |
| Paper | Minecraft 1.21.10, build 130 (`8043efd`) | TESTED | Core enable, restart, diagnostics, addon registration, optional-plugin server loading, graceful shutdown |
| Paper | Minecraft 1.21.11 (server probe: build 132, `c5eb079`) | PLAYER QA TESTED | Real-player core commands/items, Block EXP, placed-block restart persistence, Vein, Area, Auto Smelt composition, fishing, bow, duplicate handling, protection, optional integrations, and Bedrock gameplay; exact player-QA build number was not separately recorded |
| Paper | Other 1.21.x builds | EXPECTED COMPATIBLE | Compiled against API 1.21.4, but no runtime claim is made for unlisted builds |
| Paper | Outside 1.21.x | Unsupported | No build or runtime validation |

The alpha support policy is deliberately narrower than a blanket "Paper 1.21.x tested" claim. The named 1.21.4/1.21.10 builds and reported 1.21.11 player runtime are the tested profiles; other 1.21.x builds require operator validation.

## Optional integrations and addons

| Component | Runtime version | Status | Evidence / remaining scope |
| --- | --- | --- | --- |
| PlaceholderAPI | 2.12.3 | PLAYER QA TESTED (core held-tool values) | Expansion registered; level, EXP, type, UUID, maximum level, and max-level-state values expanded for a held GrowthTool. No-tool and every ability placeholder remain `MANUAL REQUIRED` |
| Vault | 1.7.3-b131 | TESTED (no-provider only) | Plugin enabled; absent economy provider produced isolated integration `ERROR` while GrowthTools stayed enabled; provider-present case is `NOT TESTED` |
| mcMMO | 2.3.000 | PLAYER QA TESTED (normal + Vein) | Normal block and Vein Miner checked without unintended additional-block EXP amplification; Area reward path remains `MANUAL REQUIRED` |
| Jobs Reborn | artifact 5.2.6.2, runtime metadata 5.2.6.1 | PLAYER QA TESTED | Normal, Vein, and Area checked with `ability-extra-block-rewards: false`; additional blocks did not multiply Jobs rewards |
| WorldEdit / WorldGuard | 7.3.9 / 7.0.13+82fdc65 | PLAYER QA TESTED | Real deny region blocked normal, Vein additional, and Area additional breaks; no protection bypass was observed |
| Geyser-Spigot | 2.11.2-SNAPSHOT (b1233) | PLAYER QA TESTED | Real Bedrock client exercised GrowthTool, Block EXP, Vein, Area, fishing, and bow |
| Floodgate | 2.2.5-SNAPSHOT (b140-8780fa4) | PLAYER QA TESTED | Real Bedrock client identity/gameplay path exercised with the same core scenario |
| Example addon | 0.1.0-SNAPSHOT against API 1.0 | TESTED (server-side partial) | API acquisition, registration, inspection, owner-disable cleanup, and idempotent close |
| Failure fixture addon | 0.1.0-SNAPSHOT against API 1.0 | TESTED (registration partial) | Broken and survivor abilities registered and were inspectable; automated exception isolation passes; player-triggered runtime exception is `MANUAL REQUIRED` |

Statuses remain scenario-scoped. A `PLAYER QA TESTED` row does not claim every plugin configuration or future version; see `manual-qa-results.md` for explicit remaining gaps.
