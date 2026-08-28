import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val baseGroup: String by project
val mcVersion: String by project
val version: String by project
val modid: String by project
val lwjgl3Version = "3.4.3"
val transformerFile = file("src/main/resources/accesstransformer.cfg")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    runConfigs {
        "client" {
            if (SystemUtils.IS_OS_MAC_OSX) {
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        if (transformerFile.exists()) {
            accessTransformer(transformerFile)
        }
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    shadowImpl("org.lwjgl:lwjgl:$lwjgl3Version")
    shadowImpl("org.lwjgl:lwjgl:$lwjgl3Version:natives-windows")
    shadowImpl("org.lwjgl:lwjgl:$lwjgl3Version:natives-linux")
    shadowImpl("org.lwjgl:lwjgl:$lwjgl3Version:natives-macos")
    shadowImpl("org.lwjgl:lwjgl:$lwjgl3Version:natives-macos-arm64")

    shadowImpl("org.lwjgl:lwjgl-nanovg:$lwjgl3Version")
    shadowImpl("org.lwjgl:lwjgl-nanovg:$lwjgl3Version:natives-windows")
    shadowImpl("org.lwjgl:lwjgl-nanovg:$lwjgl3Version:natives-linux")
    shadowImpl("org.lwjgl:lwjgl-nanovg:$lwjgl3Version:natives-macos")
    shadowImpl("org.lwjgl:lwjgl-nanovg:$lwjgl3Version:natives-macos-arm64")
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.withType(org.gradle.jvm.tasks.Jar::class) {
    archiveBaseName.set(modid)
    manifest.attributes.run {
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        if (transformerFile.exists()) {
            this["FMLAT"] = "${modid}_at.cfg"
        }
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("mcversion", mcVersion)
    inputs.property("modid", modid)
    inputs.property("basePackage", baseGroup)

    filesMatching(listOf("mcmod.info")) {
        expand(inputs.properties)
    }

    rename("accesstransformer.cfg", "META-INF/${modid}_at.cfg")
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    from(tasks.shadowJar)
    input.set(tasks.shadowJar.get().archiveFile)
    classpath(files(configurations.compileClasspath.get().filter { !it.name.startsWith("lwjgl") }))
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
}

val relocatedLwjglPath = "$baseGroup.deps.org.lwjgl".replace(".", "/")

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("non-obfuscated-with-deps")
    configurations = listOf(shadowImpl)
    exclude("**/module-info.class", "META-INF/versions/**")
    relocate(com.github.jengelman.gradle.plugins.shadow.relocation.SimpleRelocator("org.lwjgl", "$baseGroup.deps.org.lwjgl", null, null))
    relocate(com.github.jengelman.gradle.plugins.shadow.relocation.SimpleRelocator("org/lwjgl", "$relocatedLwjglPath", null, null, true))
}

tasks.assemble.get().dependsOn(tasks.remapJar)
