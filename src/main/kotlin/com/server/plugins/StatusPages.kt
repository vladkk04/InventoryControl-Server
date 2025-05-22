package com.server.plugins

import com.server.exceptions.DatabaseException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*


fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
        }

        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Unknown error")
        }

        exception<DatabaseException> { call, e ->
            when (e) {
                is DatabaseException.DuplicateKeyException -> {
                    call.respond(HttpStatusCode.Conflict, e.message)
                }
                is DatabaseException.NotFoundException -> {
                    call.respond(HttpStatusCode.NotFound, e.message)
                }
                is DatabaseException.OperationException -> {
                    call.respond(HttpStatusCode.Conflict, e.message)
                }
                is DatabaseException.ValidationException -> {
                    call.respond(HttpStatusCode.BadRequest, e.message)
                }
                is DatabaseException.GenerationException -> {
                    call.respond(HttpStatusCode.Conflict, e.cause?.message ?: e.message ?: "Something went wrong")
                }
            }
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Something went wrong")
        }
    }
}

private fun getDuplicateKeyErrorMessage(detail: Pair<String?, String?>): String {

    val (fieldName, fieldValue) = detail

    return when {
        fieldName != null && fieldValue != null ->
            "'$fieldValue' already exists for field"

        fieldName != null ->
            "Field '$fieldName' must be unique"

        else ->
            "Duplicate record already exists"
    }
}

