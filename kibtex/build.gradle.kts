import org.islandoftex.checkcites.build.Versions

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
        //all {
        //    languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        //}
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("io.kotest:kotest-assertions-core:${Versions.kotest}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:${Versions.kotest}")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation("io.kotest:kotest-framework-engine:${Versions.kotest}")
            }
        }
    }
}

tasks {
    named<Test>("jvmTest") {
        useJUnitPlatform()
    }
}
