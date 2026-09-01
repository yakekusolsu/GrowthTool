# Initial commit plan

No commit is created by this plan.

## Recommendation

Use one honest initial commit named `Initial public source import`. Because the repository has no prior tracked history, manufacturing separate Phase 1–7 commits would imply history that did not occur in Git.

Include the public source and project metadata shown by `git add --dry-run .`: `.github/`, `src/`, `docs/`, `examples/`, `api-baseline/`, Gradle wrapper/build files, `.gitignore`, license/notices, READMEs, changelog, contribution and security policies.

Exclude generated and local state: `.gradle/`, `build/`, `run/`, nested example builds, logs, databases and sidecars, diagnostics, downloaded server/plugin JARs, IDE metadata, crash files, and temporary test output.

Before the human-created commit:

```shell
git status --short --ignored
git add --dry-run .
./gradlew releaseBuild --warning-mode all
git add .
git diff --cached --stat
git diff --cached --check
git commit -m "Initial public source import"
```

Review the staged file list before the final `git commit`. Do not copy anything from the ignored `run/` profiles into the commit.
