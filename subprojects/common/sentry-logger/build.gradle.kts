plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    api(project(":subprojects:common:sentry"))
    api(project(":subprojects:common:logger"))
}
