@file:Suppress("UnstableApiUsage")

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import com.avito.instrumentation.InstrumentationTestsPlugin
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.instrumentation.reservation.request.Device
import com.avito.instrumentation.reservation.request.Device.CloudEmulator
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.KubernetesCredentials.Service
import com.avito.utils.gradle.kubernetesCredentials
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    dependencies {
        classpath("com.avito.android:buildscript")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("com.android.application") apply false
    id("com.avito.android.instrumentation-tests") apply false
    id("com.avito.android.impact")
    id("com.avito.android.build-verdict")
}

/**
 * We use exact version to provide consistent environment and avoid build cache issues
 * (AGP tasks has artifacts from build tools)
 */
val buildTools = "29.0.3"
val javaVersion = JavaVersion.VERSION_1_8
val compileSdk = 29

subprojects {
    plugins.withType<AppPlugin> {
        configure<BaseExtension> {
            packagingOptions {
                exclude("META-INF/*.kotlin_module")
            }
        }
    }

    plugins.matching { it is AppPlugin || it is LibraryPlugin }.whenPluginAdded {
        configure<BaseExtension> {
            sourceSets {
                named("main").configure { java.srcDir("src/main/kotlin") }
                named("androidTest").configure { java.srcDir("src/androidTest/kotlin") }
                named("test").configure { java.srcDir("src/test/kotlin") }
            }

            buildToolsVersion(buildTools)
            compileSdkVersion(compileSdk)

            compileOptions {
                sourceCompatibility = javaVersion
                targetCompatibility = javaVersion
            }

            defaultConfig {
                minSdkVersion(21)
                targetSdkVersion(28)
            }

            lintOptions {
                isAbortOnError = false
                isWarningsAsErrors = true
                textReport = true
                isQuiet = true
            }
        }
    }

    plugins.withType<InstrumentationTestsPlugin> {
        extensions.getByType<GradleInstrumentationPluginConfiguration>().apply {

            //todo make these params optional features in plugin
            reportApiUrl = project.getOptionalStringProperty("avito.report.url") ?: "http://stub"
            reportApiFallbackUrl = project.getOptionalStringProperty("avito.report.fallbackUrl") ?: "http://stub"
            reportViewerUrl = project.getOptionalStringProperty("avito.report.viewerUrl") ?: "http://stub"
            registry = project.getOptionalStringProperty("avito.registry", "registry") ?: "registry"
            sentryDsn = project.getOptionalStringProperty("avito.instrumentaion.sentry.dsn")
                ?: "http://stub-project@stub-host/0"
            slackToken = project.getOptionalStringProperty("avito.slack.token") ?: "stub"
            fileStorageUrl = project.getOptionalStringProperty("avito.fileStorage.url") ?: "http://stub"

            logcatTags = setOf(
                "UITestRunner:*",
                "ActivityManager:*",
                "ReportTestListener:*",
                "StorageJsonTransport:*",
                "TestReport:*",
                "VideoCaptureListener:*",
                "TestRunner:*",
                "SystemDialogsManager:*",
                "AndroidJUnitRunner:*",
                "ito.android.de:*", //по этому тэгу система пишет логи об использовании hidden/restricted api https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
                "*:E"
            )

            instrumentationParams = mapOf(
                "videoRecording" to "failed",
                "jobSlug" to "FunctionalTests"
            )


            val runAllFilterName = "runAll"
            filters.register(runAllFilterName)

            filters.register("dynamicFilter") {
                val includeAnnotation: String? = project.getOptionalStringProperty("includeAnnotation", true)
                if (includeAnnotation != null) {
                    fromSource.includeByAnnotations(setOf(includeAnnotation))
                }
                val includePrefix: String? = project.getOptionalStringProperty("includePrefix", true)
                if (includePrefix != null) {
                    fromSource.includeByPrefixes(setOf(includePrefix))
                }
            }

            filters.register("ci") {
                fromSource.excludeFlaky = true
            }

            val defaultFilter = "default"
            val customFilter: String = project.getOptionalStringProperty("customFilter", defaultFilter)

            configurationsContainer.register("Local") {
                reportSkippedTests = true
                filter = customFilter

                targetsContainer.register("api28") {
                    deviceName = "API28"

                    scheduling {
                        quota {
                            retryCount = 1
                            minimumSuccessCount = 1
                        }

                        testsCountBasedReservation {
                            device = Device.LocalEmulator.device(28, "Android_SDK_built_for_x86_64")
                            maximum = 1
                            minimum = 1
                            testsPerEmulator = 1
                        }
                    }
                }
            }

            val credentials = project.kubernetesCredentials
            if (credentials is Service || credentials is KubernetesCredentials.Config) {

                val registry = project.providers.gradleProperty("avito.registry")
                    .forUseAtConfigurationTime()
                    .orNull

                val emulator22 = CloudEmulator(
                    name = "api22",
                    api = 22,
                    model = "Android_SDK_built_for_x86",
                    image = "${emulatorImage(registry, 22)}:740eb9a948",
                    cpuCoresRequest = "1",
                    cpuCoresLimit = "1.3",
                    memoryLimit = "4Gi"
                )

                val emulator29 = CloudEmulator(
                    name = "api29",
                    api = 29,
                    model = "Android_SDK_built_for_x86_64",
                    image = "${emulatorImage(registry, 29)}:915c1f20be",
                    cpuCoresRequest = "1",
                    cpuCoresLimit = "1.3",
                    memoryLimit = "4Gi"
                )

                configurationsContainer.register("ui") {
                    reportSkippedTests = true
                    filter = customFilter

                    targetsContainer.register("api22") {
                        deviceName = "API22"

                        scheduling {
                            quota {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            testsCountBasedReservation {
                                device = emulator22
                                maximum = 50
                                minimum = 2
                                testsPerEmulator = 3
                            }
                        }
                    }

                    targetsContainer.register("api29") {
                        deviceName = "API29"

                        scheduling {
                            quota {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            testsCountBasedReservation {
                                device = emulator29
                                maximum = 50
                                minimum = 2
                                testsPerEmulator = 3
                            }
                        }
                    }
                }

                configurationsContainer.register("uiDebug") {
                    reportSkippedTests = false
                    enableDeviceDebug = true
                    filter = customFilter

                    targetsContainer.register("api29") {
                        deviceName = "API29"

                        scheduling {
                            quota {
                                retryCount = 1
                                minimumSuccessCount = 1
                            }

                            testsCountBasedReservation {
                                device = emulator29
                                maximum = 1
                                minimum = 1
                                testsPerEmulator = 1
                            }
                        }
                    }
                }

            }
        }
    }

    plugins.withType<KotlinBasePluginWrapper> {
        this@subprojects.run {
            tasks {
                withType<KotlinCompile> {
                    kotlinOptions {
                        jvmTarget = javaVersion.toString()
                        allWarningsAsErrors = false // we use deprecation a lot, and it's a compiler warning
                    }
                }
            }

            dependencies(closureOf<DependencyHandler> {
                add("implementation", Dependencies.kotlinStdlib)
            })
        }
    }
}

//todo registry value not respected here, it's unclear how its used (in fact concatenated in runner)
//todo pass whole image, and not registry
fun emulatorImage(registry: String?, api: Int): String {
    return if (registry.isNullOrBlank()) {
        "avitotech/android-emulator-$api"
    } else {
        "android/emulator-$api"
    }
}

tasks.withType<Wrapper> {
    // Sources unavailable with BIN until https://youtrack.jetbrains.com/issue/IDEA-231667 resolved
    distributionType = Wrapper.DistributionType.ALL
    gradleVersion = "6.8.2"
}
