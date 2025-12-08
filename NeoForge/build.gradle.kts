plugins {
    id("com.gradleup.shadow")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val minecraftVersion = project.properties["minecraft_version"] as String

configurations {
    create("common")
    "common" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    create("shadowBundle")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentNeoForge").extendsFrom(configurations["common"])
    "shadowBundle" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

loom {
    runs.create("datagen") {
        data()
        programArgs("--all", "--mod", "herbalcurative")
        programArgs("--output", project(":Common").file("src/main/generated/resources").absolutePath)
        programArgs("--existing", project(":Common").file("src/main/resources").absolutePath)
    }
}

dependencies {
    neoForge("net.neoforged:neoforge:${project.properties["neoforge_version"]}")

    "common"(project(":Common", "namedElements")) { isTransitive = false }
    "shadowBundle"(project(":Common", "transformProductionNeoForge"))

    // Optional WTHIT/Jade support
    modCompileOnly("mcp.mobius.waila:wthit-api:neo-12.8.2")
    // Uncomment for runtime testing with WTHIT
    // modLocalRuntime("mcp.mobius.waila:wthit:neo-12.8.2")
    // modLocalRuntime("lol.bai:badpackets:neo-0.8.2")
}

tasks {
    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", minecraftVersion)
        inputs.property("minecraft_version_range", project.properties["minecraft_version_range"])
        inputs.property("neoforge_version", project.properties["neoforge_version"])
        inputs.property("loader_version_range", project.properties["loader_version_range"])
        inputs.property("mod_id", project.properties["mod_id"])
        inputs.property("mod_name", project.properties["mod_name"])
        inputs.property("mod_license", project.properties["mod_license"])
        inputs.property("mod_version", project.version)
        inputs.property("mod_authors", project.properties["mod_authors"])
        inputs.property("mod_description", project.properties["mod_description"])

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf(
                "minecraft_version" to minecraftVersion,
                "minecraft_version_range" to project.properties["minecraft_version_range"],
                "neo_version" to project.properties["neoforge_version"],
                "loader_version_range" to project.properties["loader_version_range"],
                "mod_id" to project.properties["mod_id"],
                "mod_name" to project.properties["mod_name"],
                "mod_license" to project.properties["mod_license"],
                "mod_version" to project.version,
                "mod_authors" to project.properties["mod_authors"],
                "mod_description" to project.properties["mod_description"]
            ))
        }
    }

    shadowJar {
        exclude("architectury.common.json", ".cache/**")
        configurations = listOf(project.configurations.getByName("shadowBundle"))
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }
}

