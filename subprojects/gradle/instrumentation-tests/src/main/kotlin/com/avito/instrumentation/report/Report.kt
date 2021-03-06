package com.avito.instrumentation.report

import com.avito.instrumentation.internal.report.InMemoryReport
import com.avito.instrumentation.internal.report.ReportImpl
import com.avito.logger.LoggerFactory
import com.avito.report.ReportsApi
import com.avito.report.model.AndroidTest
import com.avito.report.model.CrossDeviceSuite
import com.avito.report.model.ReportCoordinates
import com.avito.report.model.TestStaticData
import com.avito.time.TimeProvider
import org.funktionale.tries.Try
import java.io.Serializable

interface Report : ReadReport {

    interface Factory : Serializable {

        sealed class Config : Serializable {

            data class ReportViewerCoordinates(
                val reportCoordinates: ReportCoordinates,
                val buildId: String
            ) : Config()

            data class ReportViewerId(
                val reportId: String
            ) : Config()

            data class InMemory(
                val id: String
            ) : Config()
        }

        fun createReport(config: Config): Report

        fun createReadReport(config: Config): ReadReport

        class StrategyFactory(
            private val factories: Map<String, Factory>
        ) : Factory, Serializable {

            override fun createReport(config: Config): Report =
                getFactory(config).createReport(config)

            override fun createReadReport(config: Config): ReadReport =
                getFactory(config).createReadReport(config)

            private fun getFactory(config: Config): Factory =
                requireNotNull(factories[config::class.java.simpleName]) {
                    "Factory for config: $config hasn't found. You must register"
                }
        }

        class InMemoryReportFactory(private val timeProvider: TimeProvider) : Factory {

            @Transient
            private var reports: MutableMap<Config.InMemory, InMemoryReport> = mutableMapOf()

            // TODO problems with serialization
            @Synchronized
            override fun createReport(config: Config): Report {
                return when (config) {
                    is Config.InMemory -> reports.getOrPut(config, {
                        InMemoryReport(
                            id = config.id,
                            timeProvider = timeProvider
                        )
                    })
                    is Config.ReportViewerCoordinates -> TODO("Unsupported type")
                    is Config.ReportViewerId -> TODO("Unsupported type")
                }
            }

            @Synchronized
            override fun createReadReport(config: Config): ReadReport {
                return when (config) {
                    is Config.InMemory -> reports.getOrPut(config, {
                        InMemoryReport(
                            id = config.id,
                            timeProvider = timeProvider
                        )
                    })
                    is Config.ReportViewerCoordinates -> TODO("Unsupported type")
                    is Config.ReportViewerId -> TODO("Unsupported type")
                }
            }
        }

        class ReportViewerFactory(
            val reportApiUrl: String,
            val reportApiFallbackUrl: String,
            val loggerFactory: LoggerFactory,
            val timeProvider: TimeProvider,
            val verboseHttp: Boolean
        ) : Factory {

            @Transient
            private lateinit var reportsApi: ReportsApi

            override fun createReport(config: Config): Report {
                return when (config) {
                    is Config.ReportViewerCoordinates -> {
                        ensureInitializedReportsApi()
                        ReportImpl(
                            reportsApi = reportsApi,
                            loggerFactory = loggerFactory,
                            reportCoordinates = config.reportCoordinates,
                            buildId = config.buildId,
                            timeProvider = timeProvider
                        )
                    }
                    else -> throwUnsupportedConfigException(config)
                }
            }

            override fun createReadReport(config: Config): ReadReport {
                return when (config) {
                    is Config.ReportViewerCoordinates -> {
                        ensureInitializedReportsApi()
                        ReadReport.ReportCoordinates(
                            reportsFetchApi = reportsApi,
                            coordinates = config.reportCoordinates
                        )
                    }
                    is Config.ReportViewerId -> {
                        ensureInitializedReportsApi()
                        ReadReport.Id(
                            reportsFetchApi = reportsApi,
                            id = config.reportId
                        )
                    }
                    is Config.InMemory -> TODO("Unsupported type")
                }
            }

            private fun throwUnsupportedConfigException(config: Config): Nothing {
                throw IllegalArgumentException("Unsupported config: $config")
            }

            private fun ensureInitializedReportsApi() {
                if (!::reportsApi.isInitialized) {
                    reportsApi = ReportsApi.create(
                        host = reportApiUrl,
                        loggerFactory = loggerFactory,
                        verboseHttp = verboseHttp
                    )
                }
            }
        }
    }

    fun tryCreate(apiUrl: String, gitBranch: String, gitCommit: String)

    fun tryGetId(): String?

    fun sendSkippedTests(skippedTests: List<Pair<TestStaticData, String>>)

    fun sendLostTests(lostTests: List<AndroidTest.Lost>)

    fun sendCompletedTest(completedTest: AndroidTest.Completed)

    fun finish()

    fun markAsSuccessful(testRunId: String, author: String, comment: String): Try<Unit>

    fun getCrossDeviceTestData(): Try<CrossDeviceSuite>

    companion object
}
