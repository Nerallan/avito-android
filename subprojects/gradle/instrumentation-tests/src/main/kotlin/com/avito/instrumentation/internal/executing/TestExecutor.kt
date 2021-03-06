package com.avito.instrumentation.internal.executing

import com.avito.instrumentation.internal.suite.model.TestWithTarget
import java.io.File

interface TestExecutor {

    fun execute(
        application: File?,
        testApplication: File,
        testsToRun: List<TestWithTarget>,
        executionParameters: ExecutionParameters,
        output: File
    )

    data class RunType(val id: String)
}
