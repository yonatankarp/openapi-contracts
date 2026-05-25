package com.yonatankarp.openapi.contracts

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class OpenApiContractsPluginFunctionalTest {
    @Test
    fun `generates server kotlin source from openapi spec`(
        @TempDir projectDir: File,
    ) {
        val dsl =
            """
            servers {
                register("greetings") {
                    spec.set("greetings.yaml")
                    packageName.set("com.example.api")
                    modelPackageName.set("com.example.api.models")
                }
            }
            """.trimIndent()

        runGeneration(projectDir, dsl, taskName = "generateGreetings")
        assertGeneratedKotlin(projectDir, specName = "greetings")
    }

    @Test
    fun `generates client kotlin source from openapi spec`(
        @TempDir projectDir: File,
    ) {
        val dsl =
            """
            clients {
                register("greetingsClient") {
                    spec.set("greetings.yaml")
                    packageName.set("com.example.client")
                    modelPackageName.set("com.example.client.models")
                }
            }
            """.trimIndent()

        runGeneration(projectDir, dsl, taskName = "generateGreetingsClient")
        assertGeneratedKotlin(projectDir, specName = "greetingsClient")
    }

    private fun runGeneration(
        projectDir: File,
        dsl: String,
        taskName: String,
    ): BuildResult {
        writeFixture(projectDir, "greetings.yaml")
        writeProjectFiles(projectDir, dsl)

        val result =
            GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(taskName, "--stacktrace")
                .withPluginClasspath()
                .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":$taskName")?.outcome)
        return result
    }

    private fun assertGeneratedKotlin(
        projectDir: File,
        specName: String,
    ) {
        val outputDir = File(projectDir, "build/generated/openapi/$specName/src/main/kotlin")
        assertTrue(outputDir.exists(), "Expected output directory at $outputDir")
        assertTrue(
            outputDir.walk().any { it.extension == "kt" },
            "Expected at least one generated .kt file under $outputDir",
        )
    }

    private fun writeFixture(
        projectDir: File,
        name: String,
    ) {
        val apiDir = File(projectDir, "src/main/resources/api").apply { mkdirs() }
        val source = javaClass.classLoader.getResourceAsStream("fixtures/$name")
            ?: error("Missing test fixture: fixtures/$name")
        File(apiDir, name).outputStream().use { out -> source.use { it.copyTo(out) } }
    }

    private fun writeProjectFiles(
        projectDir: File,
        dsl: String,
    ) {
        File(projectDir, "settings.gradle.kts").writeText("rootProject.name = \"functional-test\"\n")
        File(projectDir, "build.gradle.kts").writeText(
            """
            plugins {
                id("com.yonatankarp.openapi.contracts")
            }

            openapiContracts {
                $dsl
            }
            """.trimIndent() + "\n",
        )
    }
}
