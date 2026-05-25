plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    alias(libs.plugins.plugin.publish)
}

group = providers.gradleProperty("group").get()
version = providers.gradleProperty("version").get()

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.openapi.generator.gradle.plugin)

    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(gradleTestKit())
}

gradlePlugin {
    website = "https://github.com/yonatankarp/openapi-contracts"
    vcsUrl = "https://github.com/yonatankarp/openapi-contracts.git"

    plugins {
        create("openapiContracts") {
            id = "com.yonatankarp.openapi.contracts"
            implementationClass = "com.yonatankarp.openapi.contracts.OpenApiContractsPlugin"
            displayName = "OpenAPI Contracts"
            description = "Opinionated wrapper around openapi-generator-gradle-plugin for Kotlin/Spring projects"
            tags = listOf("openapi", "codegen", "kotlin", "spring")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
