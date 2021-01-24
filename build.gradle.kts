import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.islandoftex.checkcites.build.CTANBuilderTask
import org.islandoftex.checkcites.build.TDSZipBuilderTask

plugins {
    kotlin("jvm") version "1.4.21-2"
    application
    id("com.github.ben-manes.versions") version "0.36.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("com.diffplug.spotless") version "5.9.0"
    id("com.diffplug.spotless-changelog") version "2.0.0"
    id("io.gitlab.arturbosch.detekt") version "1.15.0"
}

group = "org.islandoftex"
val moduleName = "$group.${project.name}"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk7"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        java { setSrcDirs(listOf("src/main/kotlin")) }
        resources { setSrcDirs(listOf("src/main/resources")) }
    }
    test {
        java { setSrcDirs(listOf("src/test/kotlin")) }
        resources { setSrcDirs(listOf("src/test/resources")) }
    }
}

application {
    applicationName = rootProject.name
    mainClassName = "$moduleName.ApplicationKt"
}

spotlessChangelog {
    changelogFile("CHANGELOG.md")
    setAppendDashSnapshotUnless_dashPrelease(true)
    ifFoundBumpBreaking("breaking change")
    tagPrefix("v")
    commitMessage("Release v{{version}}")
    remote("origin")
    branch("master")
}

version = spotlessChangelog.versionNext
allprojects {
    repositories {
        jcenter()
    }

    group = "org.islandoftex.checkcites"
    version = rootProject.version
}


spotless {
    kotlinGradle {
        target("build.gradle.kts",
               "buildSrc/build.gradle.kts")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    kotlin {
        target("buildSrc/src/**/*.kt",
               "src/**/*.kt")
        ktlint()
        licenseHeader("// SPDX-License-Identifier: BSD-3-Clause")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

val appManifest: Manifest by extra(DefaultManifest(
    (project as ProjectInternal)
        .fileResolver
).apply {
    attributes["Implementation-Title"] = project.name
    attributes["Implementation-Version"] = project.version
    attributes["Main-Class"] = "$moduleName.ApplicationKt"
    if (java.sourceCompatibility < JavaVersion.VERSION_1_9) {
        attributes["Automatic-Module-Name"] = moduleName
    }
})

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn," +
                    "kotlin.io.path.ExperimentalPathApi")
        }
    }
    named<JavaExec>("run") {
        if (JavaVersion.current() > JavaVersion.VERSION_1_8) {
            doFirst {
                jvmArgs = listOf("--module-path", classpath.asPath)
            }
        }
    }
    named<Jar>("jar") {
        manifest.attributes.putAll(appManifest.attributes)
        archiveAppendix.set("jdk" + java.targetCompatibility.majorVersion)
    }
    named<ShadowJar>("shadowJar") {
        manifest.attributes.putAll(appManifest.attributes)
        archiveAppendix.set("jdk" + java.targetCompatibility.majorVersion + "-with-deps")
        archiveClassifier.set("")
    }
    named<DependencyUpdatesTask>("dependencyUpdates") {
        resolutionStrategy {
            componentSelection {
                all {
                    fun isNonStable(version: String) = listOf("alpha", "beta", "rc", "cr", "m", "preview", "b", "ea")
                        .any { qualifier ->
                            version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                        }
                    if (isNonStable(candidate.version) && !isNonStable(currentVersion)) {
                        reject("Release candidate")
                    }
                }
            }
        }
        checkForGradleUpdate = false
    }
    named<Task>("assembleDist").configure { dependsOn("shadowJar") }

    register<TDSZipBuilderTask>("assembleTDSZip")
    register<CTANBuilderTask>("assembleCTAN") {
        dependsOn(":assembleTDSZip")
    }
}

detekt {
    failFast = false
    buildUponDefaultConfig = true
    config = files("detekt-config.yml")
}
