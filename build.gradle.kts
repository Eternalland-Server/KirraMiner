plugins {
    `java-library`
    id("io.izzel.taboolib") version "1.34"
    id("org.jetbrains.kotlin.jvm") version "1.5.30"
}

taboolib {
    description {
        dependencies {
            @Suppress("SpellCheckingInspection")
            name("Zaphkiel")
            name("JustMessage")
        }
    }
    install("common")
    install("common-5")
    install("module-lang")
    install("module-configuration")
    install("module-chat")
    install("module-nms", "module-nms-util")
    install("platform-bukkit")
    install("expansion-command-helper")
    install("module-database")
    classifier = null
    version = "6.0.8-3"
}

repositories {
    maven {
        credentials {
            username = "a5phyxia"
            password = "zxzbc13456"
        }
        url = uri("https://maven.ycraft.cn/repository/maven-snapshots/")
    }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.tabooproject.org/repository/maven-releases/") }
    maven { url = uri("https://maven.fastmirror.net/repositories/minecraft/") }
    mavenCentral()
}

dependencies {
    compileOnly("net.sakuragame.eternal:Waypoints:1.0.0-SNAPSHOT@jar")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.3.1")
    compileOnly("net.sakuragame.eternal:JustLevel:1.1.8-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:JustMessage:1.0.4-SNAPSHOT@jar")
    compileOnly("net.sakuragame.eternal:GemsEconomy:4.9.5-SNAPSHOT@jar")
    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}