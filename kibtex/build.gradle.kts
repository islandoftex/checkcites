import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("multiplatform")
}

kotlin {
    explicitApi()

    jvm()
    js {
        browser {
            testTask {
                enabled = false
            }
        }
    }

    sourceSets {
        val versions = org.islandoftex.checkcites.build.Versions
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:${versions.jupiter}")
            }
        }
    }
}

tasks {
    named<Test>("jvmTest") {
        useJUnitPlatform()

        testLogging {
            events(
                TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED,
                TestLogEvent.STANDARD_ERROR, TestLogEvent.STANDARD_OUT
            )
        }
    }
}
