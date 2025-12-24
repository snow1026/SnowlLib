plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
    implementation(project(":snowlib-core"))
}
