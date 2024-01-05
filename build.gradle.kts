import org.jetbrains.gradle.ext.Gradle
import org.jetbrains.gradle.ext.RunConfigurationContainer
import java.util.*

plugins {
    id("java-library")
    id("maven-publish")
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
    id("eclipse")
    id("com.gtnewhorizons.retrofuturagradle") version "1.3.19"
}

// Project properties
group = "github.kasuminova.novaeng"
version = "1.8.0"

// Set the toolchain version to decouple the Java we run Gradle with from the Java used to compile and run the mod
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        // Azul covers the most platforms for Java 8 toolchains, crucially including MacOS arm64
        vendor.set(JvmVendorSpec.AZUL)
    }
    // Generate sources and javadocs jars when building and publishing
    withSourcesJar()
    withJavadocJar()
}

// Most RFG configuration lives here, see the JavaDoc for com.gtnewhorizons.retrofuturagradle.MinecraftExtension
minecraft {
    mcVersion.set("1.12.2")

    // Username for client run configurations
    username.set("Kasumi_Nova")

    // Generate a field named VERSION with the mod version in the injected Tags class
    injectedTags.put("VERSION", project.version)

    // If you need the old replaceIn mechanism, prefer the injectTags task because it doesn't inject a javac plugin.
    // tagReplacementFiles.add("RfgExampleMod.java")

    // Enable assertions in the mod's package when running the client or server
    val args = mutableListOf("-ea:${project.group}")

    // Mixin args
    args.add("-Dmixin.hotSwap=true")
    args.add("-Dmixin.checks.interfaces=true")
    args.add("-Dmixin.debug.export=true")
    extraRunJvmArguments.addAll(args)

    // If needed, add extra tweaker classes like for mixins.
    // extraTweakClasses.add("org.spongepowered.asm.launch.MixinTweaker")

    // Exclude some Maven dependency groups from being automatically included in the reobfuscated runs
    groupsToExcludeFromAutoReobfMapping.addAll("com.diffplug", "com.diffplug.durian", "net.industrial-craft")
}

// Generates a class named rfg.examplemod.Tags with the mod version in it, you can find it at
tasks.injectTags.configure {
    outputClassName.set("${project.group}.Tags")
}

// Put the version from gradle into mcmod.info
tasks.processResources.configure {
//    inputs.property("version", project.version)
//
//    filesMatching("mcmod.info") {
//        expand(mapOf("version" to project.version))
//    }
}

tasks.compileJava.configure {
    sourceCompatibility = "17"
    options.release = 8
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation

    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.compileTestJava.configure {
    sourceCompatibility = "17"
    options.release = 8
    options.encoding = "UTF-8" // Use the UTF-8 charset for Java compilation

    javaCompiler = javaToolchains.compilerFor {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.javadoc.configure {
    // No need for JavaDoc.
    actions = Collections.emptyList()
}

//tasks.jar.configure {
//    manifest {
//        val attributes = manifest.attributes
//        attributes["FMLAT"] = "novaeng_core_at.cfg"
//    }
//}

// Create a new dependency type for runtime-only dependencies that don't get included in the maven publication
val runtimeOnlyNonPublishable: Configuration by configurations.creating {
    description = "Runtime only dependencies that are not published alongside the jar"
    isCanBeConsumed = false
    isCanBeResolved = false
}
listOf(configurations.runtimeClasspath, configurations.testRuntimeClasspath).forEach {
    it.configure {
        extendsFrom(
                runtimeOnlyNonPublishable
        )
    }
}

//tasks.deobfuscateMergedJarToSrg.configure {
//    accessTransformerFiles.from("src/main/resources/META-INF/novaeng_core_at.cfg")
//}
//tasks.srgifyBinpatchedJar.configure {
//    accessTransformerFiles.from("src/main/resources/META-INF/novaeng_core_at.cfg")
//}

// Dependencies
repositories {
    flatDir {
        dirs("lib")
    }
    maven {
        url = uri("https://maven.aliyun.com/nexus/content/groups/public/")
    }
    maven {
        url = uri("https://maven.aliyun.com/nexus/content/repositories/jcenter")
    }
    maven {
        url = uri("https://maven.cleanroommc.com")
    }
    maven {
        url = uri("https://cfa2.cursemaven.com")
    }
    maven {
        url = uri("https://cursemaven.com")
    }
    maven {
        url = uri("https://maven.blamejared.com/")
    }
    maven {
        url = uri("https://repo.spongepowered.org/maven")
    }
    maven {
        name = "GeckoLib"
        url = uri("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/")
    }
    maven {
        name = "OvermindDL1 Maven"
        url = uri("https://gregtech.overminddl1.com/")
        mavenContent {
            excludeGroup("net.minecraftforge") // missing the `universal` artefact
        }
    }
    maven {
        name = "GTNH Maven"
        url = uri("http://jenkins.usrv.eu:8081/nexus/content/groups/public/")
        isAllowInsecureProtocol = true
    }
}

//mixin {
//    add sourceSets.main, "mixins.novaeng_core.refmap.json"
//}

dependencies {
    annotationProcessor("com.github.bsideup.jabel:jabel-javac-plugin:0.4.2")
    compileOnly("com.github.bsideup.jabel:jabel-javac-plugin:0.4.2")
    // workaround for https://github.com/bsideup/jabel/issues/174
    annotationProcessor("net.java.dev.jna:jna-platform:5.13.0")
    // Allow jdk.unsupported classes like sun.misc.Unsafe, workaround for JDK-8206937 and fixes Forge crashes in tests.
    patchedMinecraft("me.eigenraven.java8unsupported:java-8-unsupported-shim:1.0.0")
    // allow Jabel to work in tests
    testAnnotationProcessor("com.github.bsideup.jabel:jabel-javac-plugin:1.0.0")
    testCompileOnly("com.github.bsideup.jabel:jabel-javac-plugin:1.0.0") {
        isTransitive = false // We only care about the 1 annotation class
    }
    testCompileOnly("me.eigenraven.java8unsupported:java-8-unsupported-shim:1.0.0")

    // Mixins
    implementation("zone.rong:mixinbooter:7.1")
    val mixin : String = modUtils.enableMixins("org.spongepowered:mixin:0.8.+", "mixins.novaeng_core.refmap.json").toString()
    api (mixin) {
        isTransitive = false
    }
    annotationProcessor("org.ow2.asm:asm-debug-all:5.2")
    annotationProcessor("com.google.guava:guava:24.1.1-jre")
    annotationProcessor("com.google.code.gson:gson:2.8.6")
    annotationProcessor (mixin) {
        isTransitive = false
    }

    // Mod Dependencies
    implementation("CraftTweaker2:CraftTweaker2-MC1120-Main:1.12-4.+")
    implementation(rfg.deobf("curse.maven:modularmachinery-community-edition-817377:4991928"))
    implementation(rfg.deobf("curse.maven:had-enough-items-557549:4810661"))
    implementation(rfg.deobf("curse.maven:the-one-probe-245211:2667280"))
    implementation(rfg.deobf("curse.maven:applied-energistics-2-223794:2747063"))
    implementation(rfg.deobf("curse.maven:tx-loader-706505:4515357"))
    implementation(rfg.deobf("curse.maven:CodeChickenLib-242818:2779848"))
    implementation(rfg.deobf("curse.maven:nuclearcraft-overhauled-336895:3862197"))
    implementation(rfg.deobf("curse.maven:industrialcraft-2-242638:3078604"))
    implementation(rfg.deobf("curse.maven:mekanism-unofficial-edition-v10-edition-840735:4464199"))
    implementation(rfg.deobf("curse.maven:RedstoneFlux-270789:2920436"))
    implementation(rfg.deobf("curse.maven:cofh-core-69162:2920433"))
    implementation(rfg.deobf("curse.maven:cofh-world-271384:2920434"))
    implementation(rfg.deobf("curse.maven:thermal-foundation-222880:2926428"))
    implementation(rfg.deobf("curse.maven:thermal-expansion-69163:2926431"))

    compileOnly(rfg.deobf("curse.maven:zenutil-401178:4394263"))
    compileOnly(rfg.deobf("curse.maven:libvulpes-236541:3801015"))
    compileOnly(rfg.deobf("curse.maven:advanced-rocketry-236542:4671856"))
    compileOnly(rfg.deobf("curse.maven:immersive-engineering-231951:2974106"))
    compileOnly(rfg.deobf("curse.maven:valkyrielib-245480:2691542"))
    compileOnly(rfg.deobf("curse.maven:environmental-tech-245453:2691536"))
    compileOnly(rfg.deobf("curse.maven:smooth-font-285742:3944565"))
    compileOnly(rfg.deobf("curse.maven:astral-sorcery-241721:3044416"))
    compileOnly(rfg.deobf("curse.maven:artisan-worktables-284351:3205284"))
    compileOnly(rfg.deobf("curse.maven:touhou-little-maid-355044:3576415"))
    compileOnly(rfg.deobf("curse.maven:ingame-info-xml-225604:2489566"))
    compileOnly(rfg.deobf("curse.maven:lunatriuscore-225605:2489549"))
    compileOnly(rfg.deobf("curse.maven:rgb-chat-702720:4092100"))
    compileOnly(rfg.deobf("software.bernie.geckolib:geckolib-forge-1.12.2:3.0.31"))
}

// Publishing to a Maven repository
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
//        // Example: publishing to the GTNH Maven repository
//        maven {
//            url = uri("http://jenkins.usrv.eu:8081/nexus/content/repositories/releases")
//            isAllowInsecureProtocol = true
//            credentials {
//                username = System.getenv("MAVEN_USER") ?: "NONE"
//                password = System.getenv("MAVEN_PASSWORD") ?: "NONE"
//            }
//        }
    }
}

// IDE Settings
//eclipse {
//    classpath {
//        isDownloadSources = true
//        isDownloadJavadoc = true
//    }
//}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
        inheritOutputDirs = true // Fix resources in IJ-Native runs
    }
    project {
        this.withGroovyBuilder {
            "settings" {
                "runConfigurations" {
                    val self = this.delegate as RunConfigurationContainer
                    self.add(Gradle("1. Run Client").apply {
                        setProperty("taskNames", listOf("runClient"))
                    })
                    self.add(Gradle("2. Run Server").apply {
                        setProperty("taskNames", listOf("runServer"))
                    })
                    self.add(Gradle("3. Run Obfuscated Client").apply {
                        setProperty("taskNames", listOf("runObfClient"))
                    })
                    self.add(Gradle("4. Run Obfuscated Server").apply {
                        setProperty("taskNames", listOf("runObfServer"))
                    })
                    /*
                    These require extra configuration in IntelliJ, so are not enabled by default
                    self.add(Application("Run Client (IJ Native, Deprecated)", project).apply {
                      mainClass = "GradleStart"
                      moduleName = project.name + ".ideVirtualMain"
                      afterEvaluate {
                        val runClient = tasks.runClient.get()
                        workingDirectory = runClient.workingDir.absolutePath
                        programParameters = runClient.calculateArgs(project).map { '"' + it + '"' }.joinToString(" ")
                        jvmArgs = runClient.calculateJvmArgs(project).map { '"' + it + '"' }.joinToString(" ") +
                          ' ' + runClient.systemProperties.map { "\"-D" + it.key + '=' + it.value.toString() + '"' }
                          .joinToString(" ")
                      }
                    })
                    self.add(Application("Run Server (IJ Native, Deprecated)", project).apply {
                      mainClass = "GradleStartServer"
                      moduleName = project.name + ".ideVirtualMain"
                      afterEvaluate {
                        val runServer = tasks.runServer.get()
                        workingDirectory = runServer.workingDir.absolutePath
                        programParameters = runServer.calculateArgs(project).map { '"' + it + '"' }.joinToString(" ")
                        jvmArgs = runServer.calculateJvmArgs(project).map { '"' + it + '"' }.joinToString(" ") +
                          ' ' + runServer.systemProperties.map { "\"-D" + it.key + '=' + it.value.toString() + '"' }
                          .joinToString(" ")
                      }
                    })
                    */
                }
                "compiler" {
                    val self = this.delegate as org.jetbrains.gradle.ext.IdeaCompilerConfiguration
                    afterEvaluate {
                        self.javac.moduleJavacAdditionalOptions = mapOf(
                                (project.name + ".main") to
                                        tasks.compileJava.get().options.compilerArgs.map { '"' + it + '"' }.joinToString(" ")
                        )
                    }
                }
            }
        }
    }
}

tasks.processIdeaSettings.configure {
    dependsOn(tasks.injectTags)
}