import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.20"
    id("com.github.johnrengelman.shadow") version "7.0.0"

    application
//    `maven-publish`
//    jacoco
}

kotlin {
    target.compilations.all {
        kotlinOptions.jvmTarget = "1.8"
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("io.ktor.locations.KtorExperimentalLocationsAPI")
            languageSettings.useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
            languageSettings.useExperimentalAnnotation("kotlin.time.ExperimentalTime")
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")

        }
    }
}
group = "com.edivad99.IDSProject"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()


}
buildscript {
    repositories {
        mavenCentral()
    }


    dependencies {
        val kotlinVersion = "1.5.20"
        classpath(kotlin("gradle-plugin", version = kotlinVersion))
        classpath(kotlin("serialization", version = kotlinVersion))
    }
}

application.mainClass.set("ApplicationKt")

dependencies {
    val ktorVersion: String by project
    val kodeinVersion: String by project
    val logbackVersion: String by project
    val loremVersion: String by project
    val kotlinxCoroutinesVersion: String by project
    val kotlinxHTMLVersion: String by project
    val jupyterVersion: String by project
    val exposedVersion: String by project
    val kotlinSerializationVersion: String by project
    val postgresVersion: String by project
//    val hikariCPVersion: String by project

    api("org.kodein.di", "kodein-di", kodeinVersion)
    api("org.kodein.di", "kodein-di-framework-ktor-server-jvm", kodeinVersion)
    api("ch.qos.logback", "logback-classic", logbackVersion)

    //kotlinx and mail and common serialization
    implementation("org.jetbrains.kotlinx", "kotlinx-html-jvm", kotlinxHTMLVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", kotlinSerializationVersion)
    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", kotlinSerializationVersion)

    //ktor
    api("io.ktor", "ktor-serialization", ktorVersion)
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    implementation ("io.ktor:ktor-client-serialization:$ktorVersion")

    //exposed DAO
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    // DB Connection pool

    //implementation("com.zaxxer:HikariCP:$hikariCPVersion")
    // DB Driver
    implementation("org.postgresql:postgresql:$postgresVersion")

    //coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinxCoroutinesVersion")


    //others utils
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")

    //Mock data
    implementation("com.thedeanda", "lorem", loremVersion)


    //tests
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter", "junit-jupiter-api", jupyterVersion)
    testImplementation("org.junit.jupiter", "junit-jupiter-engine", jupyterVersion)

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
tasks.create("stage") {
    dependsOn("installDist")
}


