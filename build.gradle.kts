import java.util.zip.ZipFile
import java.security.MessageDigest

plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.modrinth.minotaur") version "2.9.0"
}

group = "dev.yakekusolsu.growthtools"
version = "0.7.0-alpha.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.helpch.at/releases")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.opencollab.dev/main/")
    maven("https://jitpack.io")
    maven("https://nexus.neetgames.com/repository/maven-public")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.13")
    compileOnly("org.geysermc.floodgate:api:2.2.4-SNAPSHOT")
    compileOnly("com.gmail.nossr50.mcMMO:mcMMO:2.3.000") { isTransitive = false }
    compileOnly("com.github.Zrips:Jobs:v5.2.6.2") { isTransitive = false }
    implementation("org.xerial:sqlite-jdbc:3.53.2.1")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.0.0")
}

val integrationTest by sourceSets.creating
integrationTest.compileClasspath += sourceSets.main.get().output
integrationTest.runtimeClasspath += sourceSets.main.get().output

configurations[integrationTest.implementationConfigurationName]
    .extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName]
    .extendsFrom(configurations.testRuntimeOnly.get())

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from("LICENSE") {
        into("META-INF")
        rename { "LICENSE-GrowthTools.txt" }
    }
    from("THIRD_PARTY_NOTICES.md") {
        into("META-INF")
    }
    from({
        configurations.runtimeClasspath.get().map { dependency ->
            if (dependency.isDirectory) dependency else zipTree(dependency)
        }
    })
    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "org/slf4j/**")
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
}

val pluginVersion = version.toString()
val modrinthToken = providers.environmentVariable("MODRINTH_TOKEN")
val modrinthProjectId = providers.environmentVariable("MODRINTH_PROJECT_ID")
    .orElse(providers.gradleProperty("modrinth.projectId"))
val modrinthChangelogFile = providers.environmentVariable("MODRINTH_CHANGELOG_FILE")
    .orElse(providers.gradleProperty("modrinth.changelogFile"))
val modrinthChangelog = providers.environmentVariable("MODRINTH_CHANGELOG")
    .orElse(modrinthChangelogFile.map { path ->
        val changelogFile = file(path)
        check(changelogFile.isFile) { "Modrinth changelog file does not exist: $path" }
        changelogFile.readText(Charsets.UTF_8)
    })
    .orElse(providers.provider {
        val changelog = file("CHANGELOG.md").readText(Charsets.UTF_8)
        val sectionStart = changelog.indexOf("## [$pluginVersion]")
        check(sectionStart >= 0) { "CHANGELOG.md has no section for $pluginVersion" }
        val nextSection = changelog.indexOf("\n## [", sectionStart + 1)
        val referenceLinks = changelog.indexOf("\n[Unreleased]:", sectionStart + 1)
        val sectionEnd = listOf(nextSection, referenceLinks)
            .filter { it >= 0 }
            .minOrNull() ?: changelog.length
        changelog.substring(sectionStart, sectionEnd).trim()
    })

modrinth {
    token.set(modrinthToken)
    projectId.set(modrinthProjectId)
    versionNumber.set(pluginVersion)
    versionName.set("GrowthTools v$pluginVersion")
    versionType.set("alpha")
    gameVersions.addAll("1.21.4", "1.21.10", "1.21.11")
    loaders.add("paper")
    changelog.set(modrinthChangelog)
    detectLoaders.set(false)
    failSilently.set(false)

    val uploadOverride = providers.environmentVariable("MODRINTH_UPLOAD_FILE").orNull
    if (uploadOverride == null) {
        uploadFile.set(tasks.jar)
    } else {
        uploadFile.set(file(uploadOverride))
    }
}

tasks.processResources {
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand("version" to pluginVersion)
    }
}

tasks.test {
    useJUnitPlatform()
}

val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs MockBukkit lifecycle and API integration tests."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

tasks.check { dependsOn(integrationTestTask) }

val apiJar = tasks.register<Jar>("apiJar") {
    archiveBaseName.set("GrowthTools-api")
    archiveClassifier.set("")
    from(sourceSets.main.get().output) {
        include("dev/yakekusolsu/growthtools/api/**")
        exclude("dev/yakekusolsu/growthtools/api/internal/**")
        exclude("dev/yakekusolsu/growthtools/api/GrowthToolsProviderAccess.class")
        exclude("dev/yakekusolsu/growthtools/api/integration/**")
        include("dev/yakekusolsu/growthtools/model/GrowthToolType.class")
    }
}

tasks.register<Jar>("apiSourcesJar") {
    archiveBaseName.set("GrowthTools-api")
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource) {
        include("dev/yakekusolsu/growthtools/api/**")
        exclude("dev/yakekusolsu/growthtools/api/internal/**")
        exclude("dev/yakekusolsu/growthtools/api/GrowthToolsProviderAccess.java")
        exclude("dev/yakekusolsu/growthtools/api/integration/**")
        include("dev/yakekusolsu/growthtools/model/GrowthToolType.java")
    }
}

val apiJavadoc = tasks.register<Javadoc>("apiJavadoc") {
    description = "Generates Javadocs for the experimental public API only."
    source = sourceSets.main.get().allJava.matching {
        include("dev/yakekusolsu/growthtools/api/**")
        exclude("dev/yakekusolsu/growthtools/api/internal/**")
        exclude("dev/yakekusolsu/growthtools/api/GrowthToolsProviderAccess.java")
        exclude("dev/yakekusolsu/growthtools/api/integration/**")
        include("dev/yakekusolsu/growthtools/model/GrowthToolType.java")
    }
    classpath = sourceSets.main.get().compileClasspath
    destinationDir = layout.buildDirectory.dir("docs/api-javadoc").get().asFile
    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        charSet = "UTF-8"
        addBooleanOption("Xdoclint:all,-missing", true)
    }
}

tasks.register<Jar>("apiJavadocJar") {
    dependsOn(apiJavadoc)
    archiveBaseName.set("GrowthTools-api")
    archiveClassifier.set("javadoc")
    from(layout.buildDirectory.dir("docs/api-javadoc"))
}

val generateApiBaseline = tasks.register("generateApiBaseline") {
    description = "Generates the public API class and method signature baseline."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn(apiJar)
    val outputFile = layout.buildDirectory.file("reports/api/public-api.txt")
    outputs.file(outputFile)
    doLast {
        val apiFile = apiJar.get().archiveFile.get().asFile
        val classes = ZipFile(apiFile).use { archive ->
            archive.entries().asSequence()
                .map { it.name }
                .filter { it.endsWith(".class") && !Regex("\\$\\d+").containsMatchIn(it) }
                .filterNot { it.endsWith("GrowthToolsProvider\$Entry.class") }
                .map { it.removeSuffix(".class").replace('/', '.') }
                .sorted()
                .toList()
        }
        val executable = javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }.get().metadata.installationPath.file(
            "bin/javap" + if (System.getProperty("os.name").startsWith("Windows")) ".exe" else ""
        ).asFile
        val process = ProcessBuilder(
            listOf(executable.absolutePath, "-public", "-s", "-classpath",
                apiFile.absolutePath) + classes
        ).redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader(Charsets.UTF_8).readText()
            .lineSequence()
            .filterNot { it.startsWith("Compiled from ") }
            .joinToString("\n", postfix = "\n")
        check(process.waitFor() == 0) { "javap failed:\n$output" }
        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(output, Charsets.UTF_8)
        }
    }
}

val verifyApiBaseline = tasks.register("verifyApiBaseline") {
    description = "Checks the public API against the 0.7.0-alpha.1 baseline."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn(generateApiBaseline)
    val baseline = layout.projectDirectory.file("api-baseline/0.7.0-alpha.1.txt")
    inputs.file(baseline)
    doLast {
        val actual = layout.buildDirectory.file("reports/api/public-api.txt").get().asFile
        val digest = MessageDigest.getInstance("SHA-256")
                .digest(actual.readBytes()).joinToString("") { "%02x".format(it) }
        check(baseline.asFile.readText(Charsets.UTF_8).trim() == digest) {
            "Public API differs from api-baseline/0.7.0-alpha.1.txt. " +
                    "Review compatibility and intentionally update the baseline."
        }
    }
}

val jarAudit = tasks.register("jarAudit") {
    description = "Audits plugin and API JAR boundaries and required resources."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    dependsOn(tasks.jar, apiJar)
    val report = layout.buildDirectory.file("reports/audits/jar-audit.txt")
    outputs.file(report)
    doLast {
        fun entries(file: File): Set<String> = ZipFile(file).use { archive ->
            archive.entries().asSequence().map { it.name }.toSet()
        }
        val pluginEntries = entries(tasks.jar.get().archiveFile.get().asFile)
        val requiredPluginEntries = setOf(
            "plugin.yml", "config.yml", "messages.yml",
            "dev/yakekusolsu/growthtools/GrowthToolsPlugin.class", "org/sqlite/JDBC.class",
            "META-INF/LICENSE-GrowthTools.txt", "META-INF/THIRD_PARTY_NOTICES.md",
            "META-INF/maven/org.xerial/sqlite-jdbc/LICENSE")
        val missing = requiredPluginEntries - pluginEntries
        check(missing.isEmpty()) { "Plugin JAR is missing: $missing" }

        val apiEntries = entries(apiJar.get().archiveFile.get().asFile)
        val forbidden = apiEntries.filter { entry ->
            entry.startsWith("dev/yakekusolsu/growthtools/api/internal/")
                    || entry.endsWith("GrowthToolsProviderAccess.class")
                    || entry.startsWith("dev/yakekusolsu/growthtools/integration/")
                    || entry.startsWith("dev/yakekusolsu/growthtools/ability/")
                    || entry.startsWith("dev/yakekusolsu/growthtools/storage/")
                    || entry.startsWith("org/sqlite/")
                    || entry.endsWith("GrowthToolsPlugin.class")
        }
        check(forbidden.isEmpty()) { "API JAR contains forbidden entries: $forbidden" }
        report.get().asFile.apply {
            parentFile.mkdirs()
            writeText("PASS\npluginEntries=${pluginEntries.size}\napiEntries=${apiEntries.size}\n",
                Charsets.UTF_8)
        }
    }
}

tasks.assemble {
    dependsOn("apiJar", "apiSourcesJar", "apiJavadocJar")
}

tasks.check {
    dependsOn(jarAudit, verifyApiBaseline)
}

val releaseBuild = tasks.register<GradleBuild>("releaseBuild") {
    description = "Runs a clean, complete, audited pre-release build."
    group = "release"
    tasks = listOf("clean", "build", "jarAudit", "verifyApiBaseline")
}

tasks.named("modrinth") {
    description = "Runs release verification and publishes only the plugin JAR to Modrinth."
    dependsOn(releaseBuild)
    doFirst {
        check(modrinthToken.orNull?.isNotBlank() == true) {
            "MODRINTH_TOKEN is required only when publishing to Modrinth."
        }
        check(modrinthProjectId.orNull?.isNotBlank() == true) {
            "Set MODRINTH_PROJECT_ID or -Pmodrinth.projectId before publishing to Modrinth."
        }
    }
}

tasks.runServer {
    minecraftVersion("1.21.11")
}
