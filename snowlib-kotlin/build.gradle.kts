plugins {
    kotlin("jvm") version "2.2.21"
    id("com.gradleup.shadow") version "8.3.0"
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(project(":snowlib-core"))
}

val targetJavaVersion = 21
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}
