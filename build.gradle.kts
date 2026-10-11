import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.6.1"
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
}

repositories {
    maven("https://maven.citizensnpcs.co/repo")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/FireML/")
    maven("https://jitpack.io")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.minebench.de")
    maven("https://repo.dmulloy2.net/nexus/repository/snapshots/")
    maven("https://repo.onarandombox.com/content/repositories/multiverse")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.onarandombox.com/content/groups/public/")
    maven("https://repo.jsinco.dev/releases")
    maven("https://mvn.wesjd.net/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("com.zaxxer:HikariCP:5.1.0")

    implementation("uk.firedev:DaisyLib:4.2")
    paperLibrary("de.themoep:minedown-adventure:1.7.6-SNAPSHOT")
    paperLibrary("de.themoep:inventorygui:1.6.7-SNAPSHOT")
    paperLibrary("net.wesjd:anvilgui:1.10.13-SNAPSHOT")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    compileOnly("com.onarandombox.multiversecore:multiverse-core:4.3.12")
    compileOnly("com.github.Zrips:CMI-API:9.8.6.4")
    compileOnly("com.acrobot.chestshop:chestshop:3.12.2")
    compileOnly("com.griefcraft:lwc:2.3.2-dev")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.dre.brewery:BreweryX:3.7.0")

    compileOnly("net.citizensnpcs:citizens-main:2.0.32-SNAPSHOT")
    compileOnly("com.denizenscript:denizen:1.2.8-SNAPSHOT")

    compileOnly("com.github.Zrips:CMILib:1.5.9.6")
    //compileOnly("uk.firedev:PoleisLib:1.0-SNAPSHOT")
    compileOnly("com.github.angeschossen:UpgradeableHoppersAPI:4.13.10")
}

group = "uk.firedev"
version = project.property("project-version") as String
java.sourceCompatibility = JavaVersion.VERSION_25

paper {
    name = project.name
    version = project.version.toString()
    main = "de.themoep.settlements.SettlementsPlugin"
    apiVersion = "26.3"
    author = "FireML"
    description = project.description.toString()

    serverDependencies {
        register("Vault") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
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

        relocate("uk.firedev.daisylib", "uk.firedev.firefly.libs.daisylib")
        relocate("com.jeff_media.customblockdata", "uk.firedev.firefly.libs.customblockdata")
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    generatePaperPluginDescription {
        useGoogleMavenCentralProxy()
    }
}
