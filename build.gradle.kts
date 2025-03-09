import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.0"
    id("org.jetbrains.compose") version "1.5.0"
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

// 明确指定 Kotlin JVM 目标版本
kotlin {
    jvmToolchain(17) // 使用 JDK 17，这是一个长期支持版本
}

// 或者使用这种方式设置 Java 兼容性
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
    implementation("com.google.code.gson:gson:2.10.1")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ADBTool"
            packageVersion = "1.0.0"
            
            windows {
                menuGroup = "ADB工具"
                upgradeUuid = "6b27e4e6-9a9d-4a7e-8b8a-f3d6e8d7b8a9"
                iconFile.set(project.file("src/main/resources/icon.ico"))
            }
        }
    }
}
