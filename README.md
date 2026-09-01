# GrowthTools

[![Build](https://github.com/yakekusolsu/GrowthTool/actions/workflows/build.yml/badge.svg)](https://github.com/yakekusolsu/GrowthTool/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Paper 1.21.x](https://img.shields.io/badge/Paper-1.21.x-blue)

[日本語](README_JA.md)

GrowthTools is an open-source Paper plugin that gives individual Minecraft tools and weapons levels, experience, and unlockable abilities. Version **0.7.0-alpha.1** is a pre-release: back up data and review the [known issues](docs/known-issues.md) before using it on a production server.

## Features

- Per-item UUID, level, total EXP, schema version, and lore stored in PDC
- Pickaxe, axe, shovel, hoe, fishing-rod, and bow GrowthTools
- Configurable EXP from blocks, fishing catches, and successful bow damage
- Vein Miner, Area Break, Auto Smelt, and Experience Boost with hard safety limits
- SQLite registry, audit log, and restart-persistent placed-block protection
- Repair, duplicate detection, explicit ID regeneration, diagnostics, and admin commands
- Experimental addon API v1 with owned ability registration and public events
- Optional, isolated adapters for PlaceholderAPI, Vault, WorldGuard, mcMMO, Jobs Reborn, Geyser, and Floodgate

Optional-integration evidence is scenario-scoped in the [compatibility matrix](docs/compatibility.md). Paper 1.21.11 player QA covers the stated WorldGuard, PlaceholderAPI, mcMMO, Jobs, Geyser, and Floodgate scenarios; unlisted configurations remain manual. Planned work includes wider compatibility testing, API stabilization, and deliberate per-tool ability choices/skill-tree design; these are not implemented features.

## Supported versions

- Paper 1.21.4 build 232 and Paper 1.21.10 build 130 have server runtime coverage
- Paper 1.21.11 is real-player QA tested for the scope in the compatibility matrix
- Other Paper 1.21.x builds are expected compatible but are not runtime-tested
- Versions outside Paper 1.21.x are unsupported
- Java 21

## Installation and quick start

1. Download `GrowthTools-0.7.0-alpha.1.jar` from a release, or build it from source.
2. Put it in the Paper server's `plugins` directory. No optional plugin is required.
3. Start the server and check the console for a successful enable message.
4. As an operator, run `/gt give <player> pickaxe`, then hold it and run `/gt inspect`.
5. Review `plugins/GrowthTools/config.yml`; use `/gt reload` after safe edits.

See the detailed [installation](docs/installation.md), [configuration](docs/configuration.md), [migration](docs/migration.md), and [privacy](docs/privacy.md) guides.

## Commands

| Command | Purpose |
| --- | --- |
| `/growthtools`, `/gt`, `/gt version` | Help and version information |
| `/gt give <player> <type>` | Create a GrowthTool |
| `/gt inspect` | Inspect the held item |
| `/gt reload` | Validate and reload configuration/messages |
| `/gt debug tool|registry|database`, `/gt debug add-level <levels>` | Administrative diagnostics and held-tool level acceleration |
| `/gt repair`, `/gt regenerate-id` | Conservative repair or explicit UUID replacement |
| `/gt ability list|info|debug` | Inspect registered abilities |
| `/gt integrations` | Show optional-integration health |
| `/gt doctor [export]` | Run or export privacy-safe diagnostics |

Types: `pickaxe`, `axe`, `shovel`, `hoe`, `fishing_rod`, `bow`. See [admin commands](docs/admin-commands.md).

## Permissions

`growthtools.command` is available to everyone. All administrative permissions default to operators: `growthtools.admin.reload`, `.give`, `.inspect`, `.debug`, `.repair`, `.regenerateid`, `.ability`, `.integrations`, and `.doctor`.

## Data and safety model

PDC is the portable source of truth; lore is display-only. SQLite stores observations and placed-block protection but never replaces valid PDC data. SQL uses a dedicated single-thread executor. If database initialization fails, PDC tools remain inspectable while registry-backed features and block EXP degrade safely. Synthetic ability breaks are bounded, skip unloaded/placed/protected blocks, and use normal durability rules.

## Developer API

Addon authors use `GrowthTools-api-0.7.0-alpha.1.jar` as `compileOnly`, declare `depend: [GrowthTools]`, and obtain API v1 through `GrowthToolsProvider.get()`. The API remains experimental during 0.x. Read the [API guide](docs/api.md), [versioning policy](docs/api-versioning.md), [ability API](docs/ability-api.md), and [example addon](examples/growthtools-example-addon/README.md).

## Building from source

```shell
./gradlew build
./gradlew releaseBuild
```

Use `gradlew.bat` on Windows. `build` runs unit and MockBukkit integration tests plus API/JAR audits. `releaseBuild` performs a clean audited distribution build. Artifacts are written to `build/libs/`. `./gradlew runServer` starts the manual QA profile on Paper 1.21.11; accepting the Minecraft EULA remains the operator's responsibility.

## Roadmap

- Phases 1–6: project foundation, progression, gameplay EXP, SQLite/registry, four abilities, addon API and optional-integration boundaries
- Phase 7: release audit, API baseline, reproducible archives, real Paper startup QA, failure isolation, and publication documentation
- Phase 7.5: disposable player QA kit, two-version Paper smoke coverage, optional-plugin lifecycle matrix, SQLite lock degradation, and Git import preparation
- Continue the unchecked player, failure, and optional-integration edge matrices in [manual QA](docs/manual-qa.md)
- Later: API stabilization and carefully modeled ability choices/skill trees

MySQL, Redis, cross-server synchronization, web dashboards, and GUIs are not included.

## Contributing and security

Contributions are welcome; read [CONTRIBUTING.md](CONTRIBUTING.md). Report vulnerabilities privately as described in [SECURITY.md](SECURITY.md), not in a public issue.

## License

GrowthTools is licensed under the [MIT License](LICENSE). Bundled third-party notices are listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
