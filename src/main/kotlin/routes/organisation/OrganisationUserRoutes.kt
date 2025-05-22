package routes.organisation

import com.server.extractPrincipalUserId
import com.server.features.organisation.invite.*
import com.server.features.organisation.invite.models.OrganisationInvitationEntity
import com.server.features.organisation.invite.models.mapToDto
import com.server.features.organisation.invite.models.mapToOrganisationUserDto
import com.server.features.organisation.invite.requests.OrganisationInvitationEmailRequest
import com.server.features.organisation.invite.requests.OrganisationInvitationUserIdRequest
import com.server.features.organisation.user.OrganisationUserAssignRoleRequest
import com.server.features.organisation.user.OrganisationUserRepository
import com.server.features.organisation.user.UpdateOrganisationUserRequest
import com.server.features.organisation.user.mapToDto
import com.server.features.user.UserRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope
import com.server.plugins.OrganisationValidationPlugin


fun Route.organisationUserRoutes() {
    route("/users") {
        route("/invite") {
            install(OrganisationValidationPlugin)
            inviteUserByEmail()
            inviteUserById()
        }

        route("/{organisationUserId}") {
            makeInactive()
            makeActive()
            editUser()
            deleteUser()
            assignRole()
        }

        getAll()
        getByUserId()

        cancelInviteByEmail()
        cancelInviteByUserId()
    }
}

private fun Route.getByUserId() {
    get("/{userId}") {
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val userId = call.parameters["userId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        return@get call.respond(organisationUserRepository.getByUserId(userId, organisationId).mapToDto())
    }
}

private fun Route.editUser() {
    put {
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val organisationUserId =
            call.parameters["organisationUserId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
        val request =
            call.receiveNullable<UpdateOrganisationUserRequest>() ?: return@put call.respond(HttpStatusCode.BadRequest)

        if (organisationUserRepository.changeOrganisationUserNameByUserId(organisationUserId, request)) {
            return@put call.respond(HttpStatusCode.OK)
        } else {
            return@put call.respond(HttpStatusCode.BadRequest)
        }
    }
}

private fun Route.assignRole() {
    post("assign-role") {
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val organisationUserId =
            call.parameters["organisationUserId"] ?: return@post call.respond(HttpStatusCode.BadRequest)

        val request = call.receiveNullable<OrganisationUserAssignRoleRequest>() ?: return@post call.respond(
            HttpStatusCode.BadRequest
        )

        if (organisationUserRepository.assignRoleByUserId(organisationUserId, request.role)) {
            return@post call.respond(HttpStatusCode.OK)
        } else {
            return@post call.respond(HttpStatusCode.BadRequest)
        }
    }
}

private fun Route.getAll() {
    get {
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        return@get call.respond(organisationUserRepository.getAllByOrganisationId(organisationId))
    }
}

private fun Route.deleteUser() {
    delete {
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val organisationInvitationRepository = call.scope.get<OrganisationInvitationRepository>()
        val organisationUserId =
            call.parameters["organisationUserId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

        organisationUserRepository.deleteById(organisationUserId)

        if (organisationInvitationRepository.deleteById(organisationUserId)) {
            return@delete call.respond(HttpStatusCode.OK)
        } else {
            return@delete call.respond(HttpStatusCode.Conflict)
        }
    }
}

private fun Route.makeInactive() {
    post("/make-inactive") {
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val organisationUserId =
            call.parameters["organisationUserId"] ?: return@post call.respond(HttpStatusCode.BadRequest)

        if (organisationUserRepository.makeUserInactive(organisationUserId)) {
            return@post call.respond(HttpStatusCode.OK)
        } else {
            return@post call.respond(HttpStatusCode.Conflict)
        }
    }
}

private fun Route.makeActive() {
    post("/make-active") {
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val organisationUserId =
            call.parameters["organisationUserId"] ?: return@post call.respond(HttpStatusCode.BadRequest)

        if (organisationUserRepository.makeUserActive(organisationUserId)) {
            return@post call.respond(HttpStatusCode.OK)
        } else {
            return@post call.respond(HttpStatusCode.Conflict)
        }
    }
}

private fun Route.cancelInviteByEmail() {
    post("cancel-invite") {
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val email = call.parameters["email"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            "Missing or malformed email"
        )

        if (organisationUserRepository.cancelInviteByEmail(email)) {
            return@post call.respond(HttpStatusCode.OK)
        } else {
            return@post call.respond(HttpStatusCode.Conflict)
        }
    }
}

private fun Route.cancelInviteByUserId() {
    post("cancel-invite") {
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val userId = call.parameters["userId"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            "Missing or malformed email"
        )

        if (organisationUserRepository.cancelInviteByUserId(userId)) {
            return@post call.respond(HttpStatusCode.OK)
        } else {
            return@post call.respond(HttpStatusCode.Conflict)
        }
    }
}

private fun Route.inviteUserByEmail() {
    post("/email") {
        val organisationInvitationRepository = call.scope.get<OrganisationInvitationRepository>()
        val userRepository = call.scope.get<UserRepository>()

        val organisationId = call.parameters["organisationId"]!!
        val userId = call.extractPrincipalUserId() ?: return@post

        val request = call.receiveNullable<OrganisationInvitationEmailRequest>() ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            "Something went wrong with request"
        )

        if (userRepository.getById(userId, "Not found user with this id").email == request.email) {
            return@post call.respond(HttpStatusCode.Conflict, "You cannot invite yourself")
        }

        val organisationUser = OrganisationInvitationEntity(
            organisationUserName = request.organisationUserName,
            organisationId = organisationId,
            organisationRole = request.organisationRole,
            email = request.email,
            invitedBy = userId,
        )

        organisationInvitationRepository.inviteAgain(organisationUser)

        return@post call.respond(HttpStatusCode.OK, organisationUser.mapToOrganisationUserDto())
    }
}

private fun Route.inviteUserById() {
    post("/userId") {
        val organisationInvitationRepository = call.scope.get<OrganisationInvitationRepository>()

        val organisationId = call.parameters["organisationId"] ?: return@post call.respond(
            HttpStatusCode.BadRequest, "Missing organisation id"
        )

        val userId = call.extractPrincipalUserId() ?: return@post

        val request = call.receiveNullable<OrganisationInvitationUserIdRequest>() ?: return@post call.respond(
            HttpStatusCode.BadRequest, "Bad Format"
        )

        if (userId == request.userId) {
            return@post call.respond(HttpStatusCode.BadRequest, "You can't invite yourself")
        }

        val organisationUser = OrganisationInvitationEntity(
            organisationUserName = request.organisationUserName,
            organisationId = organisationId,
            organisationRole = request.organisationRole,
            userId = request.userId,
            invitedBy = userId,
        )

        organisationInvitationRepository.inviteAgain(organisationUser)

        return@post call.respond(HttpStatusCode.OK, organisationUser.mapToOrganisationUserDto())
    }
}
