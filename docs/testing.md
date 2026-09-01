# Testing

Use Java 21.

```shell
./gradlew test
./gradlew integrationTest
./gradlew build
./gradlew releaseBuild
```

`test` runs domain, validation, failure-isolation, and SQLite tests. `integrationTest` runs the MockBukkit lifecycle/API contract scenario. `build` also verifies the public API baseline and both JAR boundaries. `releaseBuild` starts from `clean` and produces plugin, API, source, and Javadoc artifacts.

For real Paper:

```shell
./gradlew runServer
```

The Gradle task targets Paper 1.21.11. Review and accept the Minecraft EULA yourself in the disposable `run/` profile. Never point the task at a production world. Copy values from `testing-config.yml` deliberately rather than replacing the repository default config, follow `player-qa.md`, and record results in `manual-qa-results.md`. Compilation is not runtime compatibility evidence.

Phase 7.5 executed Paper 1.21.4 build 232 startup/shutdown and server-side loading on Paper 1.21.10 for the exact optional-plugin versions in `compatibility.md`. Later real Paper 1.21.11 human-player QA exercised the core gameplay, protection, held-tool placeholders, reported external rewards, and Bedrock scenarios recorded in `manual-qa-results.md`.

The Block EXP regression investigation also launched clean Paper 1.21.11 build 132 on Java 21 and inspected runtime tags/default configuration. Later player QA confirmed the reported pickaxe ore/stone, axe log, and shovel dirt/sand gameplay paths. Crop maturity, every fallback Material, custom datapacks, and the full wrong-tool matrix remain scoped separately rather than inferred from those passes.
