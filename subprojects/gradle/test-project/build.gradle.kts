plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(Dependencies.Test.okhttpMockWebServer)
    api(gradleTestKit())

    implementation(project(":gradle:process"))
    implementation(project(":gradle:android"))
    implementation(project(":common:truth-extensions"))
    implementation(project(":common:logger-test-fixtures"))

    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.funktionaleTry)
    implementation(Dependencies.Test.truth)

    testImplementation(Dependencies.Test.kotlinTest)
    testImplementation(Dependencies.Test.kotlinTestJUnit)
}
