plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.test)

    implementation(project(":moviekt-core"))

    api(libs.slf4j)
}

group = "tech.software-kitchen.moviekt"
description = "MovieKt-Animation"

sourceSets {
    main {
        kotlin {
            srcDirs( "./src/main" )
        }
    }
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}
