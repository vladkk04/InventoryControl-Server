
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.shadow)
}

group = "com.server"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")

    ktor {
        fatJar {
            archiveFileName.set("fat.jar")
        }
    }
    tasks {
        shadowJar {
            manifest {
                attributes(Pair("Main-Class", "io.ktor.server.netty.EngineMain"))
            }
        }
    }
}

repositories {
    mavenCentral()
    maven {url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")}
}

dependencies {
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.request.validation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)

    implementation("aws.sdk.kotlin:s3:1.4.7")

    /*implementation(libs.aws.sdk)*/

    //implementation(libs.aws.sdk.s3)

    implementation(libs.mongodb.driver)
    implementation(libs.mongo.bson)

    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.logging)
    //implementation("io.ktor:ktor-client-cio-jvm:3.1.2")

    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
