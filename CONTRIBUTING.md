# Contributing to GrowthTools

Thank you for helping GrowthTools. Search existing issues/PRs first, discuss public API or large feature changes before implementation, and keep each change focused.

## Build and test

Use Java 21 and the included Gradle wrapper:

```shell
./gradlew test
./gradlew integrationTest
./gradlew build
./gradlew releaseBuild
```

Add focused tests for changed behavior. Use `./gradlew runServer` with a disposable world and `docs/manual-qa.md` for real Paper behavior. Never call compilation a runtime compatibility test, and report exact versions/scenarios.

## Style

- Keep the plugin main class limited to lifecycle wiring.
- Keep domain logic independent of Bukkit/Paper where practical.
- Never block Paper's main thread with SQL, network, or file work.
- Avoid deprecated APIs, reflection, unnecessary dependencies, and user-facing hard-coded text.
- Treat `api` and its generated signature baseline as compatibility boundaries.
- Do not update the baseline hash until the public diff has been deliberately reviewed.
- Follow existing naming/formatting and avoid unrelated refactors.

## Pull requests

Complete the PR template with motivation, compatibility impact, and tests actually performed. Update English/Japanese documentation together for user-visible changes. Leave unperformed QA unchecked.

## Issues and security

Bug reports should include GrowthTools, Paper, Java, and optional-integration versions plus a sanitized `/gt doctor export`. Do not post vulnerabilities publicly; follow `SECURITY.md`.

Contributions are licensed under the MIT License.
