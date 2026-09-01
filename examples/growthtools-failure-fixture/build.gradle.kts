plugins { java }

group = "dev.yakekusolsu.growthtools.examples"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly(files("../../build/libs/GrowthTools-api-0.7.0-alpha.1.jar"))
}

java { toolchain.languageVersion.set(JavaLanguageVersion.of(21)) }

val pluginVersion = version.toString()
tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to pluginVersion) }
}
