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
val lwjgl3Version = "3.3.6"
val transformerFile = file("src/main/resources/accesstransformer.cfg")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            property("mixin.debug", "true")
            property("fml.coreMods.load", "com.mrailouis.kosovoclient.core.KosovoLoadingPlugin")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
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
        mixinConfig("mixins.$modid.json")
        if (transformerFile.exists()) {
            accessTransformer(transformerFile)
        }
    }
    mixin {
        defaultRefmapName.set("mixins.$modid.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

val lwjgl3: Configuration by configurations.creating

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

val relocateLwjgl by tasks.registering(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
    archiveClassifier.set("lwjgl3-relocated")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    configurations = listOf(lwjgl3)
    exclude("**/module-info.class", "META-INF/versions/**")
    relocate("org.lwjgl.BufferUtils", "$baseGroup.deps.lwjgl3root.BufferUtils")
    relocate("org.lwjgl.CLongBuffer", "$baseGroup.deps.lwjgl3root.CLongBuffer")
    relocate("org.lwjgl.PointerBuffer", "$baseGroup.deps.lwjgl3root.PointerBuffer")
    relocate("org.lwjgl.Version", "$baseGroup.deps.lwjgl3root.Version")
    relocate("org.lwjgl.VersionImpl", "$baseGroup.deps.lwjgl3root.VersionImpl")
    relocate("org.lwjgl.opengl.GL", "$baseGroup.render.GLBridge")
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")

    lwjgl3("org.lwjgl:lwjgl:$lwjgl3Version")
    lwjgl3("org.lwjgl:lwjgl:$lwjgl3Version:natives-windows")
    lwjgl3("org.lwjgl:lwjgl:$lwjgl3Version:natives-linux")
    lwjgl3("org.lwjgl:lwjgl:$lwjgl3Version:natives-macos")
    lwjgl3("org.lwjgl:lwjgl:$lwjgl3Version:natives-macos-arm64")

    lwjgl3("org.lwjgl:lwjgl-nanovg:$lwjgl3Version")
    lwjgl3("org.lwjgl:lwjgl-nanovg:$lwjgl3Version:natives-windows")
    lwjgl3("org.lwjgl:lwjgl-nanovg:$lwjgl3Version:natives-linux")
    lwjgl3("org.lwjgl:lwjgl-nanovg:$lwjgl3Version:natives-macos")
    lwjgl3("org.lwjgl:lwjgl-nanovg:$lwjgl3Version:natives-macos-arm64")

    shadowImpl(files(relocateLwjgl.flatMap { it.archiveFile }))
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
    dependsOn(relocateLwjgl)
}

tasks.withType(org.gradle.jvm.tasks.Jar::class) {
    archiveBaseName.set(modid)
    manifest.attributes.run {
        this["FMLCorePlugin"] = "$baseGroup.core.KosovoLoadingPlugin"
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ForceLoadAsMod"] = "true"
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.$modid.json"
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

    filesMatching(listOf("mcmod.info", "mixins.$modid.json")) {
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

tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("non-obfuscated-with-deps")
    configurations = listOf(shadowImpl)
    exclude("**/module-info.class", "META-INF/versions/**")
}

tasks.assemble.get().dependsOn(tasks.remapJar)
