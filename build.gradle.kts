import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.9-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.0.0-beta4" apply false
    java
    idea
    `maven-publish`
}

val minecraftVersion = project.properties["minecraft_version"] as String
architectury.minecraft = minecraftVersion

allprojects {
    version = project.properties["mod_version"] as String
    group = project.properties["maven_group"] as String
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    base.archivesName.set(project.properties["archives_base_name"] as String + "-${project.name}")

    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")
    loom.silentMojangMappingsLicense()

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://maven.parchmentmc.org")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.architectury.dev/")
        maven("https://api.modrinth.com/maven") {
            content { includeGroup("maven.modrinth") }
        }
        maven("https://cursemaven.com") {
            content { includeGroup("curse.maven") }
        }
        maven("https://maven2.bai.lol") {
            content {
                includeGroup("lol.bai")
                includeGroup("mcp.mobius.waila")
            }
        }
    }

    @Suppress("UnstableApiUsage")
    dependencies {
        "minecraft"("com.mojang:minecraft:$minecraftVersion")
        "mappings"(loom.layered {
            officialMojangMappings()
            parchment("org.parchmentmc.data:parchment-$minecraftVersion:${project.properties["parchment"]}@zip")
        })

        compileOnly("org.jetbrains:annotations:26.0.1")
        
        // WTHIT/Jade API support (compileOnly for optional compatibility)
        "modCompileOnly"("mcp.mobius.waila:wthit-api:fabric-12.8.2")
    }

    java {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(21)
        options.encoding = "UTF-8"
    }

    publishing {
        publications.create<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }

        repositories {
            mavenLocal()
            maven {
                url = uri(rootProject.file("repo"))
            }
        }
    }
}

// Disable jar task for root project (only subprojects should produce jars)
tasks.withType<Jar> {
    enabled = false
}

// IDEA configuration
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

