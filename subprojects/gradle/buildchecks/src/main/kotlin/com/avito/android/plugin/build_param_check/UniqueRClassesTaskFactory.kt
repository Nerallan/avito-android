package com.avito.android.plugin.build_param_check

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.build.gradle.tasks.ProcessApplicationManifest
import com.android.build.gradle.tasks.ProcessTestManifest
import com.avito.android.androidAppExtension
import com.avito.android.isAndroidApp
import com.avito.android.plugin.build_param_check.BuildChecksExtension.Check
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

internal class UniqueRClassesTaskFactory(
    private val rootProject: Project,
    private val config: Check.UniqueRClasses
) {

    init {
        check(rootProject.isRoot()) {
            "Project ${rootProject.path} must be root"
        }
    }

    fun register(rootTask: TaskProvider<Task>) {
        rootProject.subprojects.forEach { module ->
            module.pluginManager.withPlugin("com.android.application") {

                module.androidAppExtension.applicationVariants.all { appVariant ->
                    rootTask.dependsOn(
                        register(module, appVariant)
                    )
                }
            }
        }
    }

    private fun register(project: Project, appVariant: ApplicationVariant): TaskProvider<UniqueRClassesTask> {
        check(project.isAndroidApp()) {
            "Project ${project.path} must be an Android application module"
        }
        return project.tasks.register<UniqueRClassesTask>("checkUniqueAndroidPackages${appVariant.name.capitalize()}") {
            group = "verification"
            description = "Verify unique R classes"

            allowedNonUniquePackageNames.set(config.allowedNonUniquePackageNames)

            val processAppManifest = project.tasks.withType(ProcessApplicationManifest::class.java)
                .first { it.variantName == appVariant.name }

            val processTestManifest = project.tasks.withType(ProcessTestManifest::class.java).first()

            appManifest.set(processAppManifest.mergedManifest)
            librariesManifests.set(processAppManifest.getManifests())
            testManifests.set(processTestManifest.getManifests())

            dependsOn(processAppManifest)
            dependsOn(processTestManifest)
        }
    }
}
