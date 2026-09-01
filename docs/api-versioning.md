# API versioning policy

The plugin follows Semantic Versioning. Developer API compatibility is also exposed as `ApiVersion(major, minor)`; GrowthTools 0.7.0-alpha.1 reports API `1.0`.

During plugin 0.x, the API is experimental and can change between minor versions. Addons must check the API major version and pin the plugin version they test. After 1.0, compatible additions may increase the API minor; breaking changes require a new API major. When practical, deprecated contracts remain for a documented transition.

`./gradlew generateApiBaseline` writes every public class/method/descriptor to `build/reports/api/public-api.txt`. `verifyApiBaseline` compares its SHA-256 with `api-baseline/0.7.0-alpha.1.txt`; `check` runs that gate and CI publishes the readable report. A changed hash requires deliberate signature review, not blind baseline replacement.

API, PDC, config, and database versions are independent. This release keeps PDC/config/schema version 1.
