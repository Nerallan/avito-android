import com.avito.instrumentation.reservation.request.Device
import com.avito.kotlin.dsl.getOptionalStringProperty
import com.avito.utils.gradle.KubernetesCredentials
import com.avito.utils.gradle.kubernetesCredentials

plugins {
    id("com.android.library")
    id("kotlin-android")
    `maven-publish`
    id("com.jfrog.bintray")
    id("com.avito.android.instrumentation-tests")
}

android {
    defaultConfig {
        testInstrumentationRunner = "com.avito.android.test.app.core.TestAppRunner"

        // TODO: describe in docs that they are updated in IDE configuration only after sync!
        testInstrumentationRunnerArguments(
            mapOf(
                "planSlug" to "AndroidTestApp",
                "jobSlug" to "InstrumentationTests",
                "runId" to "123"
            )
        )
    }

    packagingOptions {
        pickFirst("META-INF/*.kotlin_module")
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

instrumentation {
    // todo make these params optional features in plugin
    reportApiUrl = "http://stub"
    reportApiFallbackUrl = "http://stub"
    reportViewerUrl = "http://stub"
    registry = "registry"
    sentryDsn = "http://stub-project@stub-host/0"
    slackToken = "stub"
    fileStorageUrl = "http://stub"

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
        "ito.android.de:*", // по этому тэгу система пишет логи об использовании hidden/restricted api https://developer.android.com/distribute/best-practices/develop/restrictions-non-sdk-interfaces
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
                    device = Device.LocalEmulator.device(
                        28,
                        "Android_SDK_built_for_x86_64"
                    )
                    maximum = 1
                    minimum = 1
                    testsPerEmulator = 1
                }
            }
        }
    }

    val credentials = project.kubernetesCredentials
    if (credentials is KubernetesCredentials.Service || credentials is KubernetesCredentials.Config) {

        @Suppress("UnstableApiUsage")
        val registry = project.providers.gradleProperty("avito.registry")
            .forUseAtConfigurationTime()
            .orNull

        val emulator22 = Device.CloudEmulator(
            name = "api22",
            api = 22,
            model = "Android_SDK_built_for_x86",
            image = "${emulatorImage(registry, 22)}:740eb9a948",
            cpuCoresRequest = "1",
            cpuCoresLimit = "1.3",
            memoryLimit = "4Gi"
        )

        val emulator29 = Device.CloudEmulator(
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

        afterEvaluate {
            val instrumentationTask = tasks.named("instrumentationUi")
            tasks.getByName("build").dependsOn(instrumentationTask)
        }
    }
}

dependencies {
    api(Dependencies.AndroidTest.core)
    api(Dependencies.AndroidTest.espressoCore)
    api(Dependencies.AndroidTest.espressoWeb)
    api(Dependencies.AndroidTest.espressoIntents)
    api(Dependencies.AndroidTest.uiAutomator)

    api(Dependencies.AndroidTest.espressoDescendantActions)

    api(Dependencies.appcompat)
    api(Dependencies.recyclerView)
    api(Dependencies.material)

    // todo implementation, waitForAssertion used in app
    api(project(":subprojects:common:waiter"))

    implementation(Dependencies.Test.hamcrestLib)
    implementation(Dependencies.Test.junit)
    implementation(Dependencies.freeReflection)

    androidTestImplementation(project(":subprojects:android-test:test-inhouse-runner"))
    androidTestImplementation(project(":subprojects:android-test:toast-rule"))

    androidTestImplementation(project(":subprojects:common:junit-utils"))
    androidTestImplementation(project(":subprojects:common:test-annotations"))

    androidTestImplementation(Dependencies.Test.truth)

    androidTestUtil(Dependencies.AndroidTest.orchestrator)
}

// todo registry value not respected here, it's unclear how its used (in fact concatenated in runner)
// todo pass whole image, and not registry
fun emulatorImage(registry: String?, api: Int): String {
    return if (registry.isNullOrBlank()) {
        "avitotech/android-emulator-$api"
    } else {
        "android/emulator-$api"
    }
}
