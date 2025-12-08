architectury {
    common("neoforge")
    platformSetupLoomIde()
}

val minecraftVersion = project.properties["minecraft_version"] as String

sourceSets.main.get().resources.srcDir("src/main/generated/resources")

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    
    // Jade API for optional Jade support
    modCompileOnly("maven.modrinth:jade:15.9.3+neoforge")
}

