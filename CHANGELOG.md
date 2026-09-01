# Changelog

Notable GrowthTools changes follow [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Added a 15–30 minute disposable player QA kit, accelerated test configuration, structured manual result ledger, initial Git commit plan, and a production-excluded failure-fixture addon.
- Recorded real server-side lifecycle checks for Paper 1.21.4/1.21.10 and available PlaceholderAPI, Vault, mcMMO, Jobs, WorldGuard, Geyser, Floodgate, and addon combinations without overstating player behavior.
- Added BlockBreakEvent regression coverage for broad pickaxe, axe, shovel, and hoe categories, wrong tools, custom reload values, crop maturity, and placed-block farming protection.
- Added `debug: true` rejection diagnostics for Block EXP decisions without enabling per-break logging in normal production configuration.
- Added `/gt debug add-level <levels>` for OP-authorized, exact held-GrowthTool level acceleration during QA, with maximum-level capping and normal audit/event updates.
- Recorded real Paper 1.21.11 human-player QA for core gameplay, placed-block persistence, Vein/Area/Auto Smelt composition, fishing, bow, UUID integrity, protection, reward isolation, placeholders, and Bedrock scope without promoting untested edge cases.

### Fixed

- Changed Vein Miner and Area Break to evaluate the current sneak state on every player-originated block break and made both bundled cooldowns zero, while preserving configurable cooldowns and recursion protection.
- Made built-in multi-block mining activation mutually exclusive per origin: configured ores prefer Vein Miner, while other compatible Pickaxe/Shovel blocks prefer Area Break.
- Applied Auto Smelt as a shared drop transformation to successful Vein Miner and Area Break additional blocks, preserving Fortune quantities, Silk Touch policy, protection gates, parent EXP, and recursion prevention without per-block activation-event spam.
- Enter degraded mode after a real SQLite operation fails, including lock contention, so later database work and block EXP fail safely until restart while PDC remains intact.
- Expanded Block EXP compatibility to a tag-first, naming-group, and compact-fallback model for standard pickaxe, axe, shovel, and hoe blocks while retaining wrong-tool rejection.
- Restricted ageable crop EXP to mature growth states while permitting mature player-planted crop harvests through placed-block farming protection.

### Changed

- Narrowed version claims to the exact tested Paper builds and marked other Paper 1.21.x builds as expected compatible rather than tested.
- Advanced the disposable `runServer` manual-QA target to Paper 1.21.11 and distinguished player-QA evidence from server lifecycle and compile-only evidence.
- Expanded `.gitignore` for logs, database sidecars, diagnostics, IDE state, crash files, and disposable test output; fixed repository line-ending policy for cross-platform Gradle wrappers.

## [0.7.0-alpha.1] - 2026-09-01

### Added

- Per-item progression for six tool/weapon types, configurable block/fishing/bow EXP, PDC lore, SQLite registry/audit storage, and restart-persistent placed-block protection.
- Vein Miner, Area Break, Auto Smelt, and Experience Boost with bounded execution, protection hooks, durability, cooldown, and unlock rules.
- Experimental API v1, public progression/ability events, owned addon ability registration, API/source/Javadoc artifacts, and an example addon.
- Optional PlaceholderAPI, Vault, WorldGuard, mcMMO, Jobs Reborn, Geyser, and Floodgate adapters with isolated health reporting.
- Administrative creation, inspection, repair, ID regeneration, integration, ability, database, and privacy-safe doctor commands.
- API signature baseline, JAR audits, reproducible archives, release build task, real Paper development profile, CI artifacts, and release/QA documentation.

### Changed

- Advanced the plugin to pre-release version `0.7.0-alpha.1`; Paper 1.21.11 is the current run-server QA target while API 1.21.4 remains the compile baseline.
- Kept concrete lifecycle, integration, storage, and built-in implementation types out of the public API JAR.
- Distinguished runtime-tested, compile-verified, detection-only, and untested compatibility claims.

### Fixed

- Isolated faulty addon conditions/executors so one failure cannot stop later abilities or the core event path.
- Removed an asynchronous read of the main-thread-owned ability registry from diagnostics.
- Restored current Paper development-server downloads with compatible Gradle/run-paper versions.

### Security

- Bounded ability identifiers, addon definitions/settings, and hostile PlaceholderAPI parameters.
- Audited database paths, prepared SQL, diagnostics filenames/content, malformed PDC handling, dependencies, artifacts, secrets, and shutdown behavior.

[Unreleased]: https://github.com/yakekusolsu/GrowthTool/commits/main
[0.7.0-alpha.1]: https://github.com/yakekusolsu/GrowthTool/releases/tag/v0.7.0-alpha.1
