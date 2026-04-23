/*
 * This file is part of a Bit libraries package.
 * Licensed under the GNU Lesser General Public License v3.0.
 *
 * Copyright (c) 2023-2026 ImBit
 */

package xyz.bitsquidd.util

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import java.util.logging.Logger


fun Project.relocate(vararg pairs: Pair<String, String>) {
    tasks.withType<ShadowJar>().configureEach {
        pairs.forEach { (from, to) ->
            run {
                relocate("nonapi.$from", to)
                relocate(from, to)
            }
        }
    }
}

fun Project.providedApi(target: Any) {
    allprojects {
        dependencies {
            add("compileOnly", target)
        }
    }
}

fun DependencyHandlerScope.shade(target: Any, transitive: Boolean = false) {
    val shadeConfig = if (transitive) "shade_internal_transitive" else "shade_internal"

    when (target) {
        is ProjectDependency -> {
            if (transitive) {
                add(shadeConfig, project(mapOf("path" to target.path)))
            } else {
                add(shadeConfig, project(mapOf("path" to target.path, "configuration" to "shadow")))
            }
        }
        else -> {
            add(shadeConfig, target)
        }
    }
}