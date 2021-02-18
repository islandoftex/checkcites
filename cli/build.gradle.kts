
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.java.archives.internal.DefaultManifest
import org.islandoftex.checkcites.build.CTANBuilderTask
import org.islandoftex.checkcites.build.TDSZipBuilderTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
    id("com.github.johnrengelman.shadow")
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk7"))
    project(":kibtex")
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

val moduleName = "org.islandoftex.checkcites"
application {
    applicationName = rootProject.name
    mainClassName = "$moduleName.ApplicationKt"
}

val appManifest: Manifest by extra(DefaultManifest(
    (project as ProjectInternal)
        .fileResolver
).apply {
    attributes["Implementation-Title"] = project.name
    attributes["Implementation-Version"] = project.version
    attributes["Main-Class"] = "ApplicationKt"
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
    named<Task>("assembleDist").configure { dependsOn("shadowJar") }

    register<TDSZipBuilderTask>("assembleTDSZip")
    register<CTANBuilderTask>("assembleCTAN") {
        dependsOn(":assembleTDSZip")
    }
}
