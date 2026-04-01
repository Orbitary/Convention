plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "2.1.1"
}

group = "xyz.bitsquidd"

java {
    disableAutoTargetJvm()
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

kotlin {
    jvmToolchain(21)
}


repositories {
    mavenCentral()
    gradlePluginPortal()
}

fun Provider<PluginDependency>.asDependency() =
    map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }


dependencies {
    implementation(libs.plugins.shadow.asDependency())
    implementation(libs.plugins.kotlin.asDependency())
    implementation(libs.plugins.errorprone.asDependency())
    implementation(gradleApi())
}

gradlePlugin {
    website.set("https://github.com/imbit/convention")
    vcsUrl.set("https://github.com/imbit/convention")

    plugins {
        create("convention") {
            id = "xyz.bitsquidd.convention"
            implementationClass = "xyz.bitsquidd.BitConventionPlugin"
            displayName = "ImBit Convention Plugin"
            description = "Shared build conventions for all ImBit projects"
            tags.set(listOf("convention", "kotlin"))
        }
    }
}