plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.6.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.24"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.minebench.de")
    maven("https://maven.citizensnpcs.co/repo")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/FireML/")
    maven("https://repo.dmulloy2.net/nexus/repository/snapshots/")
    maven("https://repo.onarandombox.com/content/repositories/multiverse")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.onarandombox.com/content/groups/public/")
    maven("https://repo.jsinco.dev/releases")
    maven("https://mvn.wesjd.net/")
    maven("https://jitpack.io")

    // OpenToast
    maven("https://repo.pauli.fyi/releases")
}

dependencies {
    paperweight.paperDevBundle("26.3.build.+")

    bukkitLibrary("com.github.ben-manes.caffeine:caffeine:3.1.8")
    bukkitLibrary("com.zaxxer:HikariCP:5.1.0")

    implementation("de.themoep:inventorygui:1.6.7-SNAPSHOT")
    bukkitLibrary("de.themoep:minedown-adventure:1.7.6-SNAPSHOT")
    bukkitLibrary("net.wesjd:anvilgui:1.10.13-SNAPSHOT")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("com.acrobot.chestshop:chestshop:3.12.2")
    compileOnly("me.clip:placeholderapi:2.11.5")

    compileOnly("net.luckperms:api:5.5")

    // Custom Things
    implementation("uk.firedev:DaisyLib:4.3-SNAPSHOT")
    compileOnly("uk.firedev:ChatChannels:1.2-SNAPSHOT")
}

group = "uk.firedev"
version = project.property("project-version") as String
java.sourceCompatibility = JavaVersion.VERSION_25

bukkit {
    name = project.name
    version = project.version.toString()
    main = "de.themoep.settlements.SettlementsPlugin"
    apiVersion = "26.3"
    author = "FireML"
    description = project.description.toString()

    depend = listOf(
        "Vault",
        "LuckPerms",
        "ChatChannels"
    )

    generateLibrariesJson = true
    paperSkipLibraries = true
    paperPluginLoader = "de.themoep.settlements.LibraryLoader"

    commands {
        register("plot") {
            description = "Manage Plots."
        }

        register("settlement") {
            description = "Settlement Commands."
            aliases = listOf("s", "village", "kingdom", "k", "v")
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.codemc.io/repository/FireML/")

            val mavenUsername = System.getenv("JENKINS_USERNAME")
            val mavenPassword = System.getenv("JENKINS_PASSWORD")

            if (mavenUsername != null && mavenPassword != null) {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["shadow"])
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveBaseName.set(project.name)
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")

        relocate("uk.firedev.daisylib", "de.themoep.settlements.libs.daisylib")
        relocate("de.themoep.inventorygui", "de.themoep.settlements.libs.inventorygui")
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    generateBukkitPluginDescription {
        useGoogleMavenCentralProxy()
    }
}
