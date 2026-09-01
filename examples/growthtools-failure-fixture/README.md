# GrowthTools failure-isolation fixture

This is a disposable QA plugin, not a production artifact. It registers a `broken` block-break executor followed by a `survivor` executor. The first deliberately throws `RuntimeException`; the second must still execute.

Build the root API first, then run `../../gradlew -p . build`. Install the fixture only in the ignored QA server. Give a test player a pickaxe GrowthTool and break one configured natural block. Expected evidence:

- GrowthTools logs one contextual error naming `growthtoolsfailurefixture:broken`.
- GrowthTools remains enabled and the server keeps running.
- The survivor logs and sends its success message.
- Built-in abilities and other addons continue.
- Disabling the fixture automatically removes both registrations; explicit handle cleanup is safe and idempotent.

Remove this fixture before any normal server use.
