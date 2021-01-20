// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class TDSZipBuilderTask : DefaultTask() {
    init {
        group = "distribution"
        description = "Create a TDS compliant ZIP file."

        inputs.files("docs", "src")
        outputs.files(project.buildDir.resolve(project.name + ".tds.zip"))
        dependsOn(":docs:buildManual", ":shadowJar")
    }

    /**
     * The task's main action: Creating a TDS zip file.
     */
    @TaskAction
    fun run() {
        logger.lifecycle("Testing required tools")
        logger.debug("Zip archive utility (zip)")
        TaskHelper.assertAvailability("zip", "-v")

        logger.lifecycle("Creating the TeX Directory Structure (TDS) archive")

        logger.info("Building the documentation directory")
        logger.debug("Creating the documentation structure")
        temporaryDir.resolve("doc/support/${project.name}").mkdirs()

        logger.debug("Copying the original documentation")
        project.copy {
            from(project.rootDir.resolve("docs"))
            into(temporaryDir.resolve("doc/support/${project.name}"))
            exclude("build.gradle.kts")
            exclude("arara.log")
        }

        logger.debug("Creating the man page directory structure")
        temporaryDir.resolve("doc/man/man1").mkdirs()

        logger.debug("Creating the man page")
        TaskHelper.createManPage(temporaryDir.resolve("doc/man/man1/${project.name}.1"), project.version.toString())

        logger.debug("Compiling the documentation")
        project.copy {
            from(project.files(project.tasks.findByPath(":docs:buildManual")))
            into(temporaryDir.resolve("doc/support/${project.name}"))
        }

        logger.debug("Copying the top level README file")
        project.file("README.md")
                .copyTo(temporaryDir.resolve("doc/support/${project.name}/README.md"),
                        overwrite = true)

        logger.info("Building the scripts directory")

        logger.debug("Creating the scripts structure")
        temporaryDir.resolve("scripts/${project.name}").mkdirs()

        logger.debug("Copying the application binary")
        project.copy {
            from(project.files(project.tasks.findByPath(":shadowJar")))
            into(temporaryDir.resolve("scripts/${project.name}"))
            rename { "${project.name}.jar" }
        }

        logger.debug("Creating the shell script wrapper")
        TaskHelper.createScript(temporaryDir.resolve("scripts/${project.name}/${project.name}.sh"))

        logger.info("Building the source code structure")

        logger.debug("Creating the source code structure")
        temporaryDir.resolve("source/support/${project.name}").mkdirs()

        logger.debug("Copying the source code directory")
        project.copy {
            from(project.rootDir.resolve("src"))
            into(temporaryDir.resolve("source/support/${project.name}"))
            exclude("build")
        }

        logger.debug("Creating the source archive file")
        TaskHelper.execute(temporaryDir.resolve("source/support/${project.name}"),
            "zip", "-r", "${project.name}-${project.version}-src.zip", "main")

        logger.debug("Removing the source code directory")
        temporaryDir.resolve("source/support/${project.name}/main").deleteRecursively()

        logger.lifecycle("Building the TDS archive file")

        logger.debug("Creating the archive file")
        TaskHelper.execute(temporaryDir, "zip", "-r", "${project.name}.tds.zip",
                "doc", "scripts", "source")

        logger.debug("Moving the archive file to the top level directory")
        temporaryDir.resolve("${project.name}.tds.zip")
                .copyTo(project.buildDir.resolve("${project.name}.tds.zip"),
                    overwrite = true)
    }
}
