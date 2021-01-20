// SPDX-License-Identifier: BSD-3-Clause
package org.islandoftex.checkcites.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class CTANBuilderTask : DefaultTask() {
    init {
        group = "distribution"
        description = "Create a CTAN-ready ZIP file."

        inputs.files(project.buildDir.resolve(project.name + ".tds.zip"))
        outputs.files(project.buildDir.resolve(project.name + ".zip"))
    }

    /**
     * The task's main action: Creating a CTAN-ready zip file.
     */
    @TaskAction
    @Suppress("TooGenericExceptionCaught")
    fun run() {
        try {
            logger.lifecycle("Testing required tools")
            logger.debug("Zip archive utility (zip)")
            TaskHelper.assertAvailability("zip", "-v")

            logger.lifecycle("Preparing the archive file for CTAN submission")

            logger.debug("Copying the TDS archive file to the temporary directory")
            project.buildDir.resolve(project.name + ".tds.zip")
                    .copyTo(temporaryDir.resolve(project.name + ".tds.zip"),
                            overwrite = true)

            logger.debug("Copying the temporary TDS structure")
            val tempTDSzip = temporaryDir.resolve(project.name + ".tds.zip")
                    .copyTo(temporaryDir.resolve("${project.name}/${project.name}.zip"))

            logger.debug("Extracting the temporary TDS structure")
            project.copy {
                from(project.zipTree(tempTDSzip))
                into(temporaryDir.resolve(project.name))
            }

            logger.debug("Removing the temporary TDS reference")
            tempTDSzip.delete()

            logger.debug("Renaming the structure")
            temporaryDir.resolve("${project.name}/doc")
                    .renameTo(temporaryDir.resolve("${project.name}/doc-old"))
            temporaryDir.resolve("${project.name}/scripts")
                    .renameTo(temporaryDir.resolve("${project.name}/scripts-old"))
            temporaryDir.resolve("${project.name}/source")
                    .renameTo(temporaryDir.resolve("${project.name}/source-old"))

            logger.debug("Copying the documentation directory")
            temporaryDir.resolve("${project.name}/doc-old/support/${project.name}")
                    .copyRecursively(temporaryDir.resolve("${project.name}/doc"))

            logger.debug("Copying the man page")
            temporaryDir.resolve("${project.name}/doc-old/man/man1/${project.name}.1")
                    .copyTo(temporaryDir.resolve("${project.name}/doc/${project.name}.1"))

            logger.debug("Removing the old documentation structure")
            temporaryDir.resolve("${project.name}/doc-old").deleteRecursively()

            logger.debug("Copying the scripts directory")
            temporaryDir.resolve("${project.name}/scripts-old/${project.name}")
                    .copyRecursively(temporaryDir.resolve("${project.name}/scripts"))

            logger.debug("Removing the old scripts structure")
            temporaryDir.resolve("${project.name}/scripts-old").deleteRecursively()

            logger.debug("Copying the source code directory")
            temporaryDir.resolve("${project.name}/source-old/support/${project.name}")
                    .copyRecursively(temporaryDir.resolve("${project.name}/source"))

            logger.debug("Removing the old source code structure")
            temporaryDir.resolve("${project.name}/source-old").deleteRecursively()

            logger.debug("Copying the README file to the top level")
            temporaryDir.resolve("${project.name}/doc/README.md")
                    .copyTo(temporaryDir.resolve("${project.name}/README.md"))

            logger.debug("Removing the original README file")
            temporaryDir.resolve("${project.name}/doc/README.md").delete()

            logger.debug("Creating the archive file")
            TaskHelper.execute(temporaryDir, "zip", "-r", "${project.name}.zip",
                    "${project.name}.tds.zip", "${project.name}")

            logger.debug("Copying archive file to top level")
            temporaryDir.resolve("${project.name}.zip")
                    .copyTo(project.buildDir.resolve("${project.name}.zip"),
                        overwrite = true)
        } catch (exception: Exception) {
            logger.error(exception.message)
        }
    }
}
