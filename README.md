# openapi-contracts

Opinionated Gradle plugin that wraps [openapi-generator](https://github.com/OpenAPITools/openapi-generator) with sensible defaults for Kotlin/Spring projects.

## Why

The OpenAPI Generator plugin is powerful but verbose — every project ends up with the same 8–10 config options for `kotlin-spring` servers and `kotlin` clients. This plugin bakes those defaults in and exposes a minimal DSL.

## Install

```kotlin
plugins {
    id("com.yonatankarp.openapi.contracts") version "0.1.0"
}
```

## Use

```kotlin
openapiContracts {
    servers {
        register("greetingsApi") {
            spec.set("greetings-api.yaml")
            packageName.set("com.example.openapi.v1")
            modelPackageName.set("com.example.openapi.v1.models")
        }
    }
    clients {
        register("paymentsClient") {
            spec.set("payments-api.yaml")
            packageName.set("com.example.client.payments")
            modelPackageName.set("com.example.client.payments.models")
        }
    }
}
```

Each registered spec produces a `generate<Name>` task. The Kotlin compile task depends on them, and generated sources are added to the `main` source set automatically.

## Defaults

### Server (`kotlin-spring`)
| Key | Value |
|---|---|
| `dateLibrary` | `java8` |
| `enumPropertyNaming` | `UPPERCASE` |
| `interfaceOnly` | `true` |
| `implicitHeaders` | `true` |
| `hideGenerationTimestamp` | `true` |
| `useTags` | `true` |
| `documentationProvider` | `none` |
| `useSpringBoot3` | `true` |
| `reactive` | `true` |

### Client (`kotlin`)
| Key | Value |
|---|---|
| `dateLibrary` | `java8` |
| `enumPropertyNaming` | `UPPERCASE` |
| `hideGenerationTimestamp` | `true` |
| `serializationLibrary` | `jackson` |
| `useCoroutines` | `true` |
| `library` | `jvm-spring-restclient` |

## Overrides

Three layers, applied in order — later wins:

```kotlin
openapiContracts {
    // 1. Override defaults globally for this project
    serverDefaults.put("reactive", "false")

    servers {
        register("greetingsApi") {
            spec.set("greetings-api.yaml")
            packageName.set("com.example")
            modelPackageName.set("com.example.models")

            // 2. Or via the typed property (clients only: `library`)
            // 3. Free-form config override per spec
            config.put("useTags", "false")
        }
    }
}
```

## Configuration

| Option | Default | Purpose |
|---|---|---|
| `directoryPath` | `src/main/resources/api` | Directory containing OpenAPI yaml files |
| `outputRoot` | `$buildDir/generated/openapi` | Root for generated code |
| `serverDefaults` | see above | Map of options applied to every server spec |
| `clientDefaults` | see above | Map of options applied to every client spec |

## License

MIT — see [LICENSE](LICENSE).
