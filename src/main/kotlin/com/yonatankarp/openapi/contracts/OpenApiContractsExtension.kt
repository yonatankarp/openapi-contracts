package com.yonatankarp.openapi.contracts

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * DSL entry point for the `com.yonatankarp.openapi.contracts` plugin.
 *
 * ```
 * openapiContracts {
 *     servers {
 *         register("greetingsApi") {
 *             spec.set("greetings-api.yaml")
 *             packageName.set("com.example.openapi.v1")
 *             modelPackageName.set("com.example.openapi.v1.models")
 *         }
 *     }
 *     clients {
 *         register("paymentsClient") {
 *             spec.set("payments-api.yaml")
 *             packageName.set("com.example.client.payments")
 *             modelPackageName.set("com.example.client.payments.models")
 *         }
 *     }
 * }
 * ```
 *
 * Each registered name becomes both the Gradle task name suffix (`generate<Name>`)
 * and the output directory under [outputRoot].
 */
abstract class OpenApiContractsExtension
    @Inject
    constructor(objects: ObjectFactory) {
        /** Directory containing OpenAPI spec yaml files. Defaults to `src/main/resources/api`. */
        abstract val directoryPath: Property<String>

        /** Output root for generated code. Defaults to `${'$'}buildDir/generated/openapi`. */
        abstract val outputRoot: Property<String>

        /** Defaults applied to every server spec. Per-spec `config` overrides individual keys. */
        abstract val serverDefaults: MapProperty<String, String>

        /** Defaults applied to every client spec. Per-spec `config` overrides individual keys. */
        abstract val clientDefaults: MapProperty<String, String>

        val servers: NamedDomainObjectContainer<ServerSpec> =
            objects.domainObjectContainer(ServerSpec::class.java)

        val clients: NamedDomainObjectContainer<ClientSpec> =
            objects.domainObjectContainer(ClientSpec::class.java)

        fun servers(action: Action<in NamedDomainObjectContainer<ServerSpec>>) = action.execute(servers)

        fun clients(action: Action<in NamedDomainObjectContainer<ClientSpec>>) = action.execute(clients)

        companion object {
            val DEFAULT_SERVER_OPTIONS =
                mapOf(
                    "dateLibrary" to "java8",
                    "enumPropertyNaming" to "UPPERCASE",
                    "interfaceOnly" to "true",
                    "implicitHeaders" to "true",
                    "hideGenerationTimestamp" to "true",
                    "useTags" to "true",
                    "documentationProvider" to "none",
                    "useSpringBoot3" to "true",
                    "reactive" to "true",
                )

            val DEFAULT_CLIENT_OPTIONS =
                mapOf(
                    "dateLibrary" to "java8",
                    "enumPropertyNaming" to "UPPERCASE",
                    "hideGenerationTimestamp" to "true",
                    "serializationLibrary" to "jackson",
                    "useCoroutines" to "true",
                    "library" to "jvm-spring-restclient",
                )
        }
    }
