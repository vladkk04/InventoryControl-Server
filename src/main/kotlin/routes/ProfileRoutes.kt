package routes

import com.server.extractPrincipalUserId
import com.server.features.auth.requests.ChangeEmailRequest
import com.server.features.auth.requests.ChangeInfoUserRequest
import com.server.features.auth.requests.ChangePasswordRequest
import com.server.features.organisation.invite.OrganisationInvitationRepository
import com.server.features.organisation.invite.models.StatusInvitation
import com.server.features.organisation.user.OrganisationUserEntity
import com.server.features.organisation.user.OrganisationUserRepository
import com.server.features.organisation.user.OrganisationUserStatus
import com.server.features.s3.S3Service
import com.server.features.security.hashing.HashingService
import com.server.features.user.UserRepository
import com.server.features.user.mapToDto
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope

fun Route.profileRoutes() {
    route("/profile") {
        route("/organisations-invitations") {
            getOrganisationsInviting()
            declineOrganisationInviting()
            acceptOrganisationInviting()
        }
        changeUserInfo()
        changePassword()
        changeEmail()
    }
}

private fun Route.changeUserInfo() {
    post("change-info") {
        val request = call.receiveNullable<ChangeInfoUserRequest>() ?: return@post call.respond(HttpStatusCode.BadRequest)
        val userRepository = call.scope.get<UserRepository>()
        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.BadRequest)
        val s3Service = call.scope.get<S3Service>()

        val user = userRepository.getById(userId, "Not found user with this id")

        if (request.logoUrl == null || user.imageUrl != request.logoUrl) {
            user.imageUrl?.let {  s3Service.deleteFile(it) }

        }

        if (userRepository.changeUserInfo(userId, request)) {
            return@post call.respond(HttpStatusCode.OK, userRepository.getById(userId, "Not found user with this id").mapToDto())
        } else {
            return@post call.respond(HttpStatusCode.Conflict)
        }
    }
}

private fun Route.changePassword() {
    post("change-password") {
        val request = call.receiveNullable<ChangePasswordRequest>() ?: return@post call.respond(HttpStatusCode.BadRequest)
        val hashingService = call.scope.get<HashingService>()
        val userRepository = call.scope.get<UserRepository>()
        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.BadRequest)

        val saltedHash = hashingService.generateSaltedHash(request.newPassword)

        if (userRepository.changePassword(userId, saltedHash)) {
            return@post call.respond(HttpStatusCode.OK)
        } else {
            return@post call.respond(HttpStatusCode.Conflict)
        }
    }
}

private fun Route.changeEmail() {
    post("change-email") {
        val request = call.receiveNullable<ChangeEmailRequest>() ?: return@post call.respond(HttpStatusCode.BadRequest, "Hello")
        val userRepository = call.scope.get<UserRepository>()
        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.BadRequest, "You are unth")


        if (userRepository.changeEmail(userId, request.email)) {
            return@post call.respond(HttpStatusCode.OK, userRepository.getById(userId, "User not found with this id").mapToDto())
        } else {
            return@post call.respond(HttpStatusCode.Conflict)
        }
    }
}

private fun Route.getOrganisationsInviting() {
    get {
        val organisationInvitationRepository = call.scope.get<OrganisationInvitationRepository>()
        val userRepository = call.scope.get<UserRepository>()
        val userId = call.extractPrincipalUserId() ?: return@get call.respond(HttpStatusCode.BadRequest)

        val user = userRepository.getById(userId, "Not found invitations for user with this id")

        return@get call.respond(
            HttpStatusCode.OK,
            organisationInvitationRepository.getAllView(user.email, userId)
        )
    }
}

private fun Route.declineOrganisationInviting() {
    post("{invitationId}/decline") {

        val invitationId = call.parameters["invitationId"] ?: return@post call.respond(HttpStatusCode.BadRequest)

        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.BadRequest)

        val organisationInvitationRepository = call.scope.get<OrganisationInvitationRepository>()

        val userRepository = call.scope.get<UserRepository>()

        val organisationInviting = organisationInvitationRepository.getById(invitationId, "Not found invitation with this id")

        val user = userRepository.getById(userId, "Not found user with this id")

        if (organisationInviting.userId != userId && organisationInviting.email != user.email) {
            return@post call.respond(HttpStatusCode.Conflict)
        } else if (organisationInviting.status == StatusInvitation.DECLINED) {
            return@post call.respond(HttpStatusCode.BadRequest, "Inviting already was decline")
        } else if (organisationInvitationRepository.updateInvitationStatus(invitationId, StatusInvitation.DECLINED)) {
            return@post call.respond(HttpStatusCode.OK, "You have successfully declined")
        } else {
            return@post call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun Route.acceptOrganisationInviting() {
    post("{invitationId}/accept") {
        val organisationInvitationRepository = call.scope.get<OrganisationInvitationRepository>()
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val userRepository = call.scope.get<UserRepository>()

        val invitationId = call.parameters["invitationId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.BadRequest)

        val organisationInviting = organisationInvitationRepository.getById(invitationId, "Not found invitation with this id")

        if (organisationInviting.userId != userId && organisationInviting.email?.let { userRepository.getByEmail(it) } == null) {
            return@post call.respond(HttpStatusCode.Conflict)
        } else if (organisationInviting.status == StatusInvitation.ACCEPTED) {
            return@post call.respond(HttpStatusCode.BadRequest, "Inviting already was accepted")
        } else if (organisationInvitationRepository.updateInvitationStatus(invitationId, StatusInvitation.ACCEPTED)) {

            val newOrganisationUserEntity = OrganisationUserEntity(
                organisationUserName = organisationInviting.organisationUserName,
                organisationId = organisationInviting.organisationId,
                organisationRole = organisationInviting.organisationRole,
                organisationUserStatus = OrganisationUserStatus.ACTIVE,
                userId = userId
            )

            organisationUserRepository.insert(newOrganisationUserEntity, "Something went wrong")

            return@post call.respond(HttpStatusCode.OK, "You have successfully accepted")
        } else {
            return@post call.respond(HttpStatusCode.NoContent)
        }
    }
}