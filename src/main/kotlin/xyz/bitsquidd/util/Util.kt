package xyz.bitsquidd.util

import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

object Util {
    fun Project.libs() = extensions
        .getByType<VersionCatalogsExtension>()
        .named("libs")

    fun VersionCatalog.plugin(alias: String): String {
        val optional = findPlugin(alias)
        check(optional.isPresent) {
            "Plugin alias '$alias' not found in version catalog. Available plugins: ${pluginAliases.joinToString()}"
        }
        return optional.get().get().pluginId
    }

    fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> {
        val optional = findLibrary(alias)
        check(optional.isPresent) {
            "Library alias '$alias' not found in version catalog. Available libraries: ${libraryAliases.joinToString()}"
        }
        return optional.get()
    }
}