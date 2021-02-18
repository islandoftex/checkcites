import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    val versions = org.islandoftex.checkcites.build.Versions
    kotlin("jvm") version versions.kotlin
    id("com.github.ben-manes.versions") version versions.versions
    id("com.diffplug.spotless") version versions.spotless
    id("com.diffplug.spotless-changelog") version versions.changelog
    id("io.gitlab.arturbosch.detekt") version versions.detekt
    id("com.github.johnrengelman.shadow") version versions.shadow apply false
}

repositories {
    jcenter()
    mavenCentral()
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
            "settings.gradle.kts",
            "buildSrc/build.gradle.kts",
            "bibtex/build.gradle.kts",
            "cli/build.gradle.kts")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    kotlin {
        target("buildSrc/src/**/*.kt",
            "bibtex/src/**/*.kt",
            "cli/src/**/*.kt")
        ktlint()
        licenseHeader("// SPDX-License-Identifier: BSD-3-Clause")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

tasks {
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
}

detekt {
    failFast = false
    input = files(
        "bibtex/src/main/kotlin",
        "cli/src/main/kotlin",
        "buildSrc/src/main/kotlin"
    )
    buildUponDefaultConfig = true
    config = files("detekt-config.yml")
}
