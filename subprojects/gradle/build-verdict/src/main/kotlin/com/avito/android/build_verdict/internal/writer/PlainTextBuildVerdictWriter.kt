package com.avito.android.build_verdict.internal.writer

import com.avito.android.build_verdict.internal.BuildVerdict
import com.avito.utils.logging.CILogger
import java.io.File

internal class PlainTextBuildVerdictWriter(
    buildVerdictDir: Lazy<File>,
    override val logger: CILogger
) : BuildVerdictWriter(buildVerdictDir, fileName) {

    override fun writeTo(buildVerdict: BuildVerdict, destination: File) {
        destination.writeText(
            when (buildVerdict) {
                is BuildVerdict.Execution -> buildVerdict.plainText()
                is BuildVerdict.Configuration -> buildVerdict.plainText()
            }
        )
    }

    companion object {
        const val fileName = "pretty-build-verdict.txt"
    }
}