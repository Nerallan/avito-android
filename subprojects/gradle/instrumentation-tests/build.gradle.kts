plugins {
    id("java-gradle-plugin")
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
    id("nebula.integtest")
}

dependencies {
    api(project(":subprojects:gradle:kubernetes"))
    api(project(":subprojects:common:time"))
    api(project(":subprojects:gradle:build-verdict-tasks-api"))

    implementation(Dependencies.commonsIo) {
        because("LogcatBuffer.Impl.tailer needs to consider Charset (https://issues.apache.org/jira/browse/IO-354)")
    }
    implementation(Dependencies.commonsText)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.gson)
    implementation(Dependencies.kotson)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.teamcityClient)
    implementation(project(":subprojects:common:composite-exception"))
    implementation(project(":subprojects:common:file-storage"))
    implementation(project(":subprojects:common:logger"))
    implementation(project(":subprojects:common:report-viewer"))
    implementation(project(":subprojects:common:retrace"))
    implementation(project(":subprojects:common:sentry"))
    implementation(project(":subprojects:common:test-annotations"))
    implementation(project(":subprojects:gradle:android"))
    implementation(project(":subprojects:gradle:gradle-logger"))
    implementation(project(":subprojects:gradle:statsd-config"))
    implementation(project(":subprojects:common:files"))
    implementation(project(":subprojects:gradle:git"))
    implementation(project(":subprojects:gradle:instrumentation-changed-tests-finder"))
    implementation(project(":subprojects:gradle:instrumentation-tests-dex-loader"))
    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:gradle:process"))
    implementation(project(":subprojects:gradle:runner:client"))
    implementation(project(":subprojects:gradle:runner:device-provider"))
    implementation(project(":subprojects:gradle:runner:stub"))
    implementation(project(":subprojects:gradle:teamcity"))
    implementation(project(":subprojects:gradle:upload-cd-build-result"))
    implementation(project(":subprojects:gradle:build-failer"))
    implementation(project(":subprojects:gradle:worker"))

    testImplementation(project(":subprojects:gradle:test-project"))
    testImplementation(project(":subprojects:gradle:slack-test-fixtures"))
    testImplementation(project(":subprojects:gradle:build-failer-test-fixtures"))
    testImplementation(project(":subprojects:common:logger-test-fixtures"))
    testImplementation(project(":subprojects:common:time-test-fixtures"))
    testImplementation(project(":subprojects:gradle:instrumentation-tests-dex-loader-test-fixtures"))
    testImplementation(project(":subprojects:common:report-viewer-test-fixtures"))
    testImplementation(project(":subprojects:common:resources"))
    testImplementation(testFixtures(project(":subprojects:gradle:runner:device-provider")))
    testImplementation(Dependencies.Test.mockitoKotlin)
    testImplementation(Dependencies.Test.mockitoJUnitJupiter)
    testImplementation(Dependencies.Test.okhttpMockWebServer)
}

gradlePlugin {
    plugins {
        create("functionalTests") {
            id = "com.avito.android.instrumentation-tests"
            implementationClass = "com.avito.instrumentation.InstrumentationTestsPlugin"
            displayName = "Instrumentation tests"
        }
    }
}
