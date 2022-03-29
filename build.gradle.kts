plugins {
    `java-library`
    id("io.izzel.taboolib") version "1.34"
    id("org.jetbrains.kotlin.jvm") version "1.5.10"
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
    version = "6.0.7-50"
}

repositories {
    maven {
        credentials {
            username = "a5phyxia"
            password = "zxzbc13456"
        }
        url = uri("https://maven.ycraft.cn/repository/maven-snapshots/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms:Zaphkiel:1.7.6@jar")
    compileOnly("net.sakuragame.eternal:JustMessage:1.0.4-SNAPSHOT@jar")
    compileOnly("ink.ptms.core:v11200:11200-minimize@jar")
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