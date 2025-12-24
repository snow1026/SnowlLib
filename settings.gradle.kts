pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "SnowLib"

include("snowlib-core")
include("snowlib-kotlin")
include("mappings:v1_21_R1")
include("mappings:v1_21_R2")
include("mappings:v1_21_R3")
include("mappings:v1_21_R4")
include("mappings:v1_21_R5")
include("mappings:v1_21_R6")
include("mappings:v1_21_R7")
