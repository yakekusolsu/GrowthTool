# GrowthTools example addon

This deliberately small Paper plugin demonstrates API discovery, read-only tool lookup,
namespaced EXP mutation, owned ability registration, an executor, event listening, and
explicit unregister on disable. GrowthTools also automatically unregisters registrations
if an addon cannot clean itself up.

Build the root project first so `../../build/libs/GrowthTools-api-0.7.0-alpha.1.jar`
exists, then run `../../gradlew -p . build` from this directory.
