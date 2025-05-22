package com.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*


suspend fun ApplicationCall.handleQueryParameter(parameterName: String): String? {
    if (this.request.queryParameters[parameterName].isNullOrEmpty()) {
        this.respond(HttpStatusCode.BadRequest, "$parameterName is required")
        return null
    } else {
        return this.request.queryParameters[parameterName]
    }
}

fun ApplicationCall.extractPrincipalUserId(): String? =
    this.principal<JWTPrincipal>()?.payload?.getClaim("userId")?.asString()