# Installation

## Requirements

- Paper 1.21.4 build 232 or Paper 1.21.10 build 130 (server runtime-tested), or Paper 1.21.11 (player-QA tested scope)
- Other Paper 1.21.x builds are expected compatible but not runtime-tested; versions outside 1.21.x are unsupported
- Java 21
- A backup before installing pre-release software

PlaceholderAPI, Vault and an economy provider, WorldGuard, mcMMO, Jobs Reborn, Geyser, and Floodgate are optional. GrowthTools starts without them.

## Install

1. Download `GrowthTools-0.7.0-alpha.1.jar` from an official release, or build it with `./gradlew releaseBuild`.
2. Stop the Paper server. Put the plugin JAR in `plugins/`; do not install the API-only JAR as a plugin.
3. Start Paper. Confirm GrowthTools enabled and created `plugins/GrowthTools/config.yml`, `messages.yml`, and `growthtools.db`.
4. Run `/gt version`, `/gt doctor`, and `/gt integrations` as an operator.
5. Run `/gt give <player> pickaxe`, hold the item, and run `/gt inspect`.
6. Stop the server before making broad configuration changes. For supported live changes, edit config/messages and run `/gt reload`.

Never use a plugin hot-reloader. Use a full server restart for JAR updates and integration changes.
