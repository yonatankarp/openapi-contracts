package com.yonatankarp.openapi.contracts

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

class OpenApiContractsPluginTest {
    @Test
    fun `applies plugin and exposes openapiContracts extension`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.yonatankarp.openapi.contracts")

        assertInstanceOf(
            OpenApiContractsExtension::class.java,
            project.extensions.findByName("openapiContracts"),
        )
    }

    @Test
    fun `registers a generate task for each server spec with opinionated defaults`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.yonatankarp.openapi.contracts")

        val ext = project.extensions.getByType(OpenApiContractsExtension::class.java)
        ext.servers.register("greetingsApi") {
            spec.set("greetings-api.yaml")
            packageName.set("com.example.openapi.v1")
            modelPackageName.set("com.example.openapi.v1.models")
        }

        val task = assertInstanceOf(GenerateTask::class.java, project.tasks.findByName("generateGreetingsApi"))
        assertEquals("kotlin-spring", task.generatorName.get())
        assertEquals("com.example.openapi.v1", task.apiPackage.get())
        assertEquals("com.example.openapi.v1.models", task.modelPackage.get())

        val cfg = task.configOptions.get()
        assertEquals("true", cfg["reactive"])
        assertEquals("true", cfg["useSpringBoot3"])
    }

    @Test
    fun `registers a generate task for each client spec with default library`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.yonatankarp.openapi.contracts")

        val ext = project.extensions.getByType(OpenApiContractsExtension::class.java)
        ext.clients.register("paymentsClient") {
            spec.set("payments-api.yaml")
            packageName.set("com.example.client.payments")
            modelPackageName.set("com.example.client.payments.models")
        }

        val task = assertInstanceOf(GenerateTask::class.java, project.tasks.findByName("generatePaymentsClient"))
        assertEquals("kotlin", task.generatorName.get())

        val cfg = task.configOptions.get()
        assertEquals("jvm-spring-restclient", cfg["library"])
        assertEquals("true", cfg["useCoroutines"])
    }

    @Test
    fun `spec-level config overrides plugin defaults`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.yonatankarp.openapi.contracts")

        val ext = project.extensions.getByType(OpenApiContractsExtension::class.java)
        ext.servers.register("greetingsApi") {
            spec.set("greetings-api.yaml")
            packageName.set("com.example")
            modelPackageName.set("com.example.models")
            config.put("reactive", "false")
        }

        val task = project.tasks.getByName("generateGreetingsApi") as GenerateTask
        assertEquals("false", task.configOptions.get()["reactive"])
    }

    @Test
    fun `client library property overrides default library`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.yonatankarp.openapi.contracts")

        val ext = project.extensions.getByType(OpenApiContractsExtension::class.java)
        ext.clients.register("paymentsClient") {
            spec.set("payments-api.yaml")
            packageName.set("com.example")
            modelPackageName.set("com.example.models")
            library.set("jvm-okhttp4")
        }

        val task = project.tasks.getByName("generatePaymentsClient") as GenerateTask
        assertEquals("jvm-okhttp4", task.configOptions.get()["library"])
    }

    @Test
    fun `plugin-level defaults can be overridden`() {
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.yonatankarp.openapi.contracts")

        val ext = project.extensions.getByType(OpenApiContractsExtension::class.java)
        ext.serverDefaults.put("reactive", "false")
        ext.servers.register("greetingsApi") {
            spec.set("greetings-api.yaml")
            packageName.set("com.example")
            modelPackageName.set("com.example.models")
        }

        val task = project.tasks.getByName("generateGreetingsApi") as GenerateTask
        assertEquals("false", task.configOptions.get()["reactive"])
    }
}
