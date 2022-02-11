interface Dependency {
    val dependency: String
}

object Versions {
    const val kotlin = "1.6.10"

    object KotlinX {
        object Coroutines {
            private const val version = "1.6.0"

            object Core : Dependency {
                override val dependency = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
            }

            object Test : Dependency {
                override val dependency = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$version"
            }
        }
    }

    object Square {
        object Retrofit : Dependency {
            private const val version = "2.9.0"
            override val dependency = "com.squareup.retrofit2:retrofit:$version"

            object Converters {
                object Moshi : Dependency {
                    override val dependency = "com.squareup.retrofit2:converter-moshi:$version"
                }
            }
        }

        object Moshi : Dependency {
            private const val version = "1.13.0"
            override val dependency = "com.squareup.moshi:moshi:$version"

            object KotlinCodegen : Dependency {
                override val dependency = "com.squareup.moshi:moshi-kotlin-codegen:$version"
            }
        }

        object OkHttp : Dependency {
            private const val version = "4.9.1"
            override val dependency = "com.squareup.okhttp3:okhttp:$version"

            object MockWebServer : Dependency {
                override val dependency = "com.squareup.okhttp3:mockwebserver:$version"
            }
        }
    }

    object KotlinLogging: Dependency {
        override val dependency = "io.github.microutils:kotlin-logging-jvm:2.1.20"
    }

    object Pf4j : Dependency {
        override val dependency = "org.pf4j:pf4j:3.4.1"
    }

    object Slf4j : Dependency {
        override val dependency = "org.slf4j:slf4j-simple:1.7.33"
    }

    object Junit {
        private const val project = "org.junit.jupiter"
        private const val version = "5.7.0"

        object Jupiter : Dependency {
            override val dependency = "$project:junit-jupiter:$version"

            object Api : Dependency {
                override val dependency = "$project:junit-jupiter-api:$version"
            }

            object Engine : Dependency {
                override val dependency = "$project:junit-jupiter-engine:$version"
            }

            object Params : Dependency {
                override val dependency = "$project:junit-jupiter-params:$version"
            }
        }
    }

    object Jsoup : Dependency {
        override val dependency = "org.jsoup:jsoup:1.13.1"
    }
}
