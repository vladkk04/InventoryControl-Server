package routes

import com.server.features.user.UserRepository
import com.server.features.user.mapToDto
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope


fun Route.userRoutes() {
    route("/users") {
        getUserRoutes()
    }
}

private fun Route.getUserRoutes() {
    get("find") {
        val userRepository = call.scope.get<UserRepository>()

        val email = call.queryParameters["email"]
        val userId = call.queryParameters["id"]

        when {
            email != null && userId != null -> {
                call.respond(HttpStatusCode.BadRequest, "Required only query parameter")
            }

            email != null -> {
                userRepository.getByEmail(email).let {
                    return@get call.respond(HttpStatusCode.OK, it.mapToDto())
                }
            }

            userId != null -> {
                userRepository.getById(userId, "Not found with this user id").let {
                    return@get call.respond(HttpStatusCode.OK, it.mapToDto())
                }
            }

            else -> {
                call.respond(HttpStatusCode.BadRequest, "Required field 'email' or 'user_id' is missing")
            }
        }
    }
}
