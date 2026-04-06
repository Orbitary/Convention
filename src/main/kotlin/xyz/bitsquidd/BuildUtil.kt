package xyz.bitsquidd

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File

object BuildUtil {
    private val STANDARD_TASKS = setOf("compileJava", "compileKotlin", "shadowJar", "sourcesJar", "javadoc", "processResources")

    fun Project.standardiseDirectories() {
        allprojects {
            registerRegularStandardisation()
            tasks.matching { it.name in STANDARD_TASKS }.configureEach {
                dependsOn("standardiseDirectories")
            }
        }

        afterEvaluate {
            tasks.register("standardiseAllDirectories") {
                group = "bit"
                description = "Standardises directories for all subprojects."
                dependsOn(
                    subprojects
                        .filter { it.tasks.findByName("standardiseDirectories") != null }
                        .map { it.tasks.named("standardiseDirectories") }
                )
            }
        }
    }


    private fun Project.registerRegularStandardisation(): TaskProvider<Task> =
        tasks.register("standardiseDirectories") {
            group = "bit"
            description = "Generates package-info.java and cleans up directories for all Java packages."

            val srcDir = project.file("src/main/java")
            val templateFile = rootProject.file("template/package-info.template")

            if (!srcDir.exists()) {
                project.logger.warn("Source directory 'src/main/java' not found in module:'${project.name}'. Skipping package-info generation.")
                return@register
            }
            if (!templateFile.exists()) {
                project.logger.warn("Template file 'template/package-info.template' not found in module:'${project.name}'. Skipping package-info generation.")
                return@register
            }

            inputs.dir(srcDir).optional(true)
            inputs.file(templateFile).optional(true)
            outputs.dir(srcDir).optional(true)

            doLast {
                deleteEmptyDirs(srcDir)
                createPackageInfo(srcDir, templateFile)
                deleteEmptyDirs(srcDir)
            }
        }

    private fun createPackageInfo(root: File, templateFile: File) {
        if (!templateFile.exists()) throw GradleException("Template file 'template/package-info.template' not found!")
        val template = templateFile.readText()

        root.walkTopDown()
            .filter { it.isDirectory && it != root }
            .forEach { dir ->
                val javaFiles = dir.listFiles()
                    ?.filter { it.isFile && it.extension == "java" && it.name != "package-info.java" }
                    ?: emptyList()
                val packageInfoFile = File(dir, "package-info.java")
                val isNoReplace = packageInfoFile.exists() &&
                  packageInfoFile.useLines { it.firstOrNull()?.startsWith("//NOREPLACE") == true }

                when {
                    isNoReplace -> return@forEach
                    javaFiles.isNotEmpty() -> {
                        val packageName = dir.relativeTo(root).path.replace(File.separatorChar, '.')
                        packageInfoFile.writeText(
                            template.replace("\${PACKAGE_NAME}", packageName)
                        )
                    }

                    packageInfoFile.exists() -> packageInfoFile.delete()
                }
            }
    }

    private fun deleteEmptyDirs(root: File) {
        if (!root.exists() || !root.isDirectory) return
        root.walkBottomUp()
            .filter { it.isDirectory && it != root }
            .forEach { dir ->
                if (dir.listFiles()?.isEmpty() == true) dir.delete()
            }
    }

}