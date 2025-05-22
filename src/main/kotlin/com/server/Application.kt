package com.server

import com.server.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = runBasicServer()

fun runBasicServer() {
    embeddedServer(Netty, port = (System.getenv("PORT")?: "5000").toInt(), module = Application::module).start(wait = true)
}

/*
fun runConfiguredServer() {
    println("Configure Server")
    embeddedServer(Netty, configure = {
        connectors.add(EngineConnectorBuilder().apply {
            port = (System.getenv("PORT")?:"5000").toInt()
        })
        connectionGroupSize = 2
        workerGroupSize = 5
        callGroupSize = 10
        shutdownGracePeriod = 2000
        shutdownTimeout = 3000
    }, module = Application::module).start(wait = true)
}

fun runServerWithCommandLineConfig(args: Array<String>) {
    println("Commanded Server")
    embeddedServer(
        factory = Netty,
        configure = {
            val cliConfig = CommandLineConfig(args)
            takeFrom(cliConfig.engineConfig)
            loadCommonConfiguration(cliConfig.rootConfig.environment.config)
        },
        module = Application::module
    ).start(wait = true)
}
*/

fun Application.module() {
    configureKoin()

    configureStatusPages()
    configureRequestValidation()
    configureSecurity()
    configureRouting()
    configureSerialization()
    configureMonitoring()
}
