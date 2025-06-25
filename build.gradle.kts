import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.jvm.tasks.Jar

plugins {
    id("java") // Tell gradle this is a java project.
    id("java-library") // Import helper for source-based libraries.
    id("com.diffplug.spotless") version "7.0.4" // Import auto-formatter.
    id("com.gradleup.shadow") version "8.3.6" // Import shadow API.
    eclipse // Import eclipse plugin for IDE integration.
}

group = "net.coreprotect"

val projectVersion = "22.4"
val projectBranch = ""

version = projectVersion

description = "Provides block protection for your server."

java { toolchain { languageVersion.set(JavaLanguageVersion.of(17)) } }

logger.info("Building version $version")

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://repo.purpurmc.org/snapshots") }
    maven("https://repo.papermc.io/repository/maven-public")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://maven.enginehub.org/repo")
}

dependencies {
    implementation(platform("com.intellectualsites.bom:bom-newest:1.45"))
    compileOnly("io.papermc.paper:paper-api:1.19-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.github.oshi:oshi-core:6.6.5")
}

tasks.named<Jar>("jar") {
    dependsOn(tasks.spotlessApply)
    archiveClassifier.set("original")
}

artifacts { add("archives", tasks.named("shadowJar")) }

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("com.zaxxer", project.group.toString())
        exclude(dependency("com.google.code.gson:.*"))
        exclude(dependency("org.intellij:.*"))
        exclude(dependency("org.jetbrains:.*"))
        exclude(dependency("org.slf4j:.*"))
    }
    archiveClassifier.set(null as String?)
}

val resourceTokens = mapOf("project.version" to projectVersion, "project.branch" to projectBranch)

tasks.named<org.gradle.api.tasks.Copy>("processResources") {
    include("plugin.yml")
    filter(mapOf("tokens" to resourceTokens, "beginToken" to "\${", "endToken" to "}"), ReplaceTokens::class.java)
}

extra["author"] = "Intelli"

extra["resourceTokens"] = resourceTokens

spotless {
    java {
        removeUnusedImports()
        palantirJavaFormat()
    }
    kotlinGradle {
        ktfmt().kotlinlangStyle().configure { it.setMaxWidth(120) }
        target("build.gradle.kts", "settings.gradle.kts")
    }
}
