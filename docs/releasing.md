# Releasing GrowthTools

This document covers the prepared GitHub Release to Modrinth publication path. It does not authorize a publication by itself. Create and validate the GitHub Release first, then publish that exact plugin artifact to Modrinth.

## Modrinth project setup

Create the Modrinth project without inventing an ID in the source tree:

- Name: `GrowthTools`
- Project type: Plugin
- Loader: Paper
- Initial version: `0.7.0-alpha.1`
- Version type: Alpha
- Supported Minecraft versions: `1.21.4`, `1.21.10`, and `1.21.11`

The listed Minecraft versions reflect documented GrowthTools test coverage. Do not describe every Paper 1.21.x build as fully tested; consult [compatibility.md](compatibility.md) for the exact runtime and player-QA scope.

After the project exists, copy its real project ID or slug from Modrinth. Do not add a placeholder ID to `build.gradle.kts`.

## Credentials and configuration

Configure these GitHub Actions repository secrets:

- `MODRINTH_TOKEN`: a Modrinth personal access token with the minimum `CREATE_VERSION` scope
- `MODRINTH_PROJECT_ID`: the real GrowthTools Modrinth project ID or slug

The token is read only from the `MODRINTH_TOKEN` environment variable and must never be committed, added to `gradle.properties`, included in diagnostics, or printed in logs. The project ID may alternatively be supplied locally through `-Pmodrinth.projectId=<real-id>`.

Normal `build`, `test`, `integrationTest`, and `releaseBuild` tasks do not require either value. Only the `modrinth` publishing task requires them.

## Publication metadata

Minotaur publishes only `GrowthTools-${version}.jar` with:

- Version number: the Gradle project version
- Version name: `GrowthTools v${version}`
- Version type: `alpha`
- Loader: `paper`
- Minecraft versions: `1.21.4`, `1.21.10`, and `1.21.11`

The API, sources, Javadocs, test fixtures, Paper server files, databases, logs, and diagnostics are not additional Modrinth files.

No Modrinth dependency relations are currently declared. PlaceholderAPI, WorldGuard, Vault, mcMMO, Jobs Reborn, Geyser, and Floodgate remain optional integrations. Add a relation only after verifying its real Modrinth project ID; never guess one.

The GitHub workflow uses the published GitHub Release body as the Modrinth changelog. A local Gradle publication may set `MODRINTH_CHANGELOG`, point `MODRINTH_CHANGELOG_FILE` or `-Pmodrinth.changelogFile` at a reviewed Markdown file, or fall back to the matching section in `CHANGELOG.md`.

## GitHub Actions publication

`.github/workflows/publish-modrinth.yml` runs only for:

- Tags matching `v*`
- An explicit `workflow_dispatch`

It does not publish on normal `main` pushes or pull requests. The job is restricted to `yakekusolsu/GrowthTool`, has read-only GitHub contents permission, checks both secrets, validates the tag against the Gradle version, requires an existing non-draft GitHub Release, downloads only `GrowthTools-${version}.jar`, verifies its SHA-256 digest when GitHub supplies one, and runs `releaseBuild` before Minotaur uploads.

A tag-triggered run fails safely when its GitHub Release has not been published yet. Publish the GitHub Release, then use `workflow_dispatch` (or rerun the failed tag workflow) after reviewing the release and configuring both secrets.

Because `v0.7.0-alpha.1` predates this workflow, its first Modrinth upload must use `workflow_dispatch` after the Modrinth project and secrets exist:

```shell
gh workflow run "Publish to Modrinth" --ref main -f release_tag=v0.7.0-alpha.1
```

Review the Actions log and the resulting Modrinth version before announcing it.

## Local manual publication

For an intentional local upload, set the token and real project ID in the process environment, then run the publishing task. The task runs `releaseBuild` first and stops before upload if verification fails.

```shell
MODRINTH_TOKEN=<secret> MODRINTH_PROJECT_ID=<real-id> ./gradlew modrinth --warning-mode all
```

On Windows PowerShell, set the two process environment variables without saving them to a file, then use `./gradlew.bat modrinth --warning-mode all`. Never paste a real token into documentation, a command transcript intended for sharing, or a committed script.
