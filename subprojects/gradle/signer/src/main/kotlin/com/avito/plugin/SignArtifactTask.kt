package com.avito.plugin

import com.avito.logger.GradleLoggerFactory
import com.avito.logger.create
import com.avito.utils.BuildFailer
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

abstract class SignArtifactTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @Input
    val tokenProperty = objects.property<String>()

    @Input
    val serviceUrl = objects.property<String>()

    protected abstract fun unsignedFile(): File

    protected abstract fun signedFile(): File

    /**
     * TODO: find a better way to profile a final artifact to CI
     * Results of transformations are stored in build/intermediates/bundle/release/<task name>/out
     * It's accessible by Artifacts API programmatically but we need a final one file.
     * We rewrite an original file to preserve legacy behaviour.
     */
    protected abstract fun hackForArtifactsApi()

    @TaskAction
    fun run() {
        val loggerFactory = GradleLoggerFactory.fromTask(this)
        val unsignedFile = unsignedFile()
        val signedFile = signedFile()

        val serviceUrl: String = serviceUrl.get()

        val signResult = SignViaServiceAction(
            serviceUrl = serviceUrl,
            token = tokenProperty.get(),
            unsignedFile = unsignedFile,
            signedFile = signedFile,
            loggerFactory = loggerFactory
        ).sign() // TODO: User workers

        hackForArtifactsApi()

        val buildFailer = BuildFailer.RealFailer()

        val logger = loggerFactory.create<SignArtifactTask>()

        signResult.fold(
            { logger.info("signed successfully: ${signedFile.path}") },
            { buildFailer.failBuild("Can't sign: ${signedFile.path};", it) }
        )
    }
}
