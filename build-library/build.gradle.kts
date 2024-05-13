plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
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

    api(libs.common.vector)
    implementation(libs.software.renderer)
    implementation(libs.svg)

    api(libs.slf4j)
}

group = "tech.software-kitchen.moviekt"
val versionStr = "0.8.0"
version = versionStr

sourceSets {
    main {
        kotlin {
            srcDirs( "../moviekt-animation/src/main" )
            srcDirs( "../moviekt-clips/src/main" )
            srcDirs( "../moviekt-core/src/main" )
            srcDirs( "../moviekt-dsl/src/main" )
            srcDirs( "../moviekt-filter/src/main" )
            srcDirs( "../moviekt-layout/src/main" )
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



publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "tech.softwarekitchen"
            artifactId = "MovieKt"
            version = versionStr

            from(components["java"])
        }
    }
}

