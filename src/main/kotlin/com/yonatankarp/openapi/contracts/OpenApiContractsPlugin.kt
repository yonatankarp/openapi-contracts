package com.yonatankarp.openapi.contracts

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask
import java.io.File
import java.util.concurrent.Callable

class OpenApiContractsPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.pluginManager.apply("org.openapi.generator")

        val ext =
            project.extensions.create<OpenApiContractsExtension>("openapiContracts").apply {
                directoryPath.convention(
                    project.layout.projectDirectory.dir("src/main/resources/api").asFile.absolutePath,
                )
                outputRoot.convention(
                    project.layout.buildDirectory.dir("generated/openapi").map { it.asFile.absolutePath },
                )
                serverDefaults.convention(OpenApiContractsExtension.DEFAULT_SERVER_OPTIONS)
                clientDefaults.convention(OpenApiContractsExtension.DEFAULT_CLIENT_OPTIONS)
            }

        ext.servers.all { project.registerServer(this, ext) }
        ext.clients.all { project.registerClient(this, ext) }

        project.registerCleanTask(ext)
        project.wireKotlinCompile(ext)
        project.wireSourceSets(ext)
    }

    private fun Project.resolveInputSpec(
        directoryPath: Provider<String>,
        specFile: Provider<String>,
    ) = layout.file(directoryPath.flatMap { dir -> specFile.map { File("$dir/$it") } })

    private fun Project.resolveOutputDir(
        outputRoot: Provider<String>,
        specName: String,
    ) = layout.dir(outputRoot.map { File("$it/$specName") })

    private fun Project.resolveTemplateDir(templatesDir: Provider<String>) =
        layout.dir(templatesDir.map { File(it) })

    private fun Project.registerServer(
        spec: ServerSpec,
        ext: OpenApiContractsExtension,
    ) {
        tasks.register<GenerateTask>(taskNameFor(spec.name)) {
            group = OPENAPI_GROUP
            description = "Generate server code for ${spec.name}"
            generatorName.set("kotlin-spring")
            inputSpec.set(resolveInputSpec(ext.directoryPath, spec.spec))
            outputDir.set(resolveOutputDir(ext.outputRoot, spec.name))
            apiPackage.set(spec.packageName)
            modelPackage.set(spec.modelPackageName)
            configOptions.set(
                ext.serverDefaults.zip(spec.config.orElse(emptyMap())) { defaults, overrides ->
                    defaults + overrides
                },
            )
            templateDir.set(resolveTemplateDir(spec.templatesDir))
        }
    }

    private fun Project.registerClient(
        spec: ClientSpec,
        ext: OpenApiContractsExtension,
    ) {
        val libraryOverride = spec.library.map { mapOf("library" to it) }.orElse(emptyMap())
        val merged =
            ext.clientDefaults
                .zip(libraryOverride) { defaults, libOverride -> defaults + libOverride }
                .zip(spec.config.orElse(emptyMap())) { merged, overrides -> merged + overrides }

        tasks.register<GenerateTask>(taskNameFor(spec.name)) {
            group = OPENAPI_GROUP
            description = "Generate client code for ${spec.name}"
            generatorName.set("kotlin")
            inputSpec.set(resolveInputSpec(ext.directoryPath, spec.spec))
            outputDir.set(resolveOutputDir(ext.outputRoot, spec.name))
            apiPackage.set(spec.packageName)
            modelPackage.set(spec.modelPackageName)
            configOptions.set(merged)
            templateDir.set(resolveTemplateDir(spec.templatesDir))
        }
    }

    private fun Project.registerCleanTask(ext: OpenApiContractsExtension) {
        val outputRoot = ext.outputRoot
        val cleanGen =
            tasks.register("cleanGeneratedOpenApiCode") {
                group = OPENAPI_GROUP
                description = "Removes generated OpenAPI code"
                doLast { File(outputRoot.get()).deleteRecursively() }
            }
        plugins.withId("base") {
            tasks.named<Task>("clean") { dependsOn(cleanGen) }
        }
    }

    private fun Project.wireKotlinCompile(ext: OpenApiContractsExtension) {
        plugins.withId("org.jetbrains.kotlin.jvm") {
            tasks.named<Task>("compileKotlin") {
                dependsOn(
                    Callable {
                        (ext.servers.names + ext.clients.names).map { taskNameFor(it) }
                    },
                )
            }
        }
    }

    private fun Project.wireSourceSets(ext: OpenApiContractsExtension) {
        extensions.findByType<SourceSetContainer>()?.named("main") {
            java.srcDir(
                provider {
                    (ext.servers.names + ext.clients.names).map {
                        "${ext.outputRoot.get()}/$it/src/main/kotlin"
                    }
                },
            )
        }
    }

    private fun taskNameFor(name: String) = "generate" + name.replaceFirstChar { it.uppercase() }

    companion object {
        private const val OPENAPI_GROUP = "openapi tools"
    }
}
