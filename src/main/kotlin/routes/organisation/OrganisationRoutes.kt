package routes.organisation

import com.server.extractPrincipalUserId
import com.server.features.organisation.*
import com.server.features.organisation.settings.OrganisationSettings
import com.server.features.organisation.settings.OrganisationSettingsRepository
import com.server.features.organisation.user.OrganisationUserEntity
import com.server.features.organisation.user.OrganisationUserRepository
import com.server.features.organisation.user.OrganisationUserStatus
import com.server.features.s3.S3Service
import com.server.features.user.UserRepository
import com.server.plugins.OrganisationValidationPlugin
import io.ktor.client.engine.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope


fun Route.organisationRoutes() {
    route("/organisations") {
        createOrganisation()
        getAllOrganisationsByUserId()

        getOrganisationById()

        route("/{organisationId}") {
            install(OrganisationValidationPlugin)

            organisationInvitationRoutes()
            organisationSettingsRoutes()
            organisationUserRoutes()
            productCategoryRoutes()
            productRoutes()
            orderRoutes()


            deleteOrganisation()
            updateOrganisation()
        }
    }
}

private fun Route.getAllOrganisationsByUserId() {
    get {
        val organisationRepository = call.scope.get<OrganisationRepository>()
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()

        val userId = call.extractPrincipalUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)

        return@get call.respond(
            HttpStatusCode.OK,
            organisationRepository.getAllByIds(
                organisationUserRepository.getAllByUserId(userId).map { it.organisationId }.toSet()
            ).map { it.mapToDto() }
        )
    }
}

private fun Route.createOrganisation() {
    post("create") {
        val request =
            call.receiveNullable<OrganisationRequest>() ?: return@post call.respond(HttpStatusCode.BadRequest)

        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)

        val userRepository = call.scope.get<UserRepository>()
        val organisationRepository = call.scope.get<OrganisationRepository>()
        val organisationUserRepository = call.scope.get<OrganisationUserRepository>()
        val organisationSettings = call.scope.get<OrganisationSettingsRepository>()

        val userName = userRepository.getById(userId, "Not found user with this id").fullName

        val organisation = Organisation(
            name = request.name,
            currency = request.currency,
            description = request.description,
            logoUrl = request.logoUrl,
            createdBy = userId
        )

        val organisationUserEntity = OrganisationUserEntity(
            organisationUserName = userName,
            organisationId = organisation.id.toHexString(),
            organisationRole = OrganisationRole.ADMIN,
            organisationUserStatus = OrganisationUserStatus.ACTIVE,
            userId = userId
        )

        val defaultOrganisationSettings = OrganisationSettings(
            organisationId = organisation.id.toHexString()
        )

        return@post if (organisationRepository.insert(organisation, "Organisation already exists")
            && organisationUserRepository.insert(organisationUserEntity, "Organisation user already exists")
            && organisationSettings.insert(defaultOrganisationSettings, "Organisation settings already exists")
        ) {
            call.respond(
                HttpStatusCode.OK,
                organisation.mapToDto()
            )
        } else {
            call.respond(HttpStatusCode.BadRequest, "Something went wrong")
        }

    }
}

private fun Route.getOrganisationById() {
    get("/{organisationId}") {
        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val organisationRepository = call.scope.get<OrganisationRepository>()

        organisationRepository.getById(organisationId, "Not found organisation with this id").let {
            return@get call.respond(HttpStatusCode.OK, it.mapToDto())
        }
    }
}

private fun Route.updateOrganisation() {
    put {
        val organisationId =
            call.parameters["organisationId"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing ID")

        val s3Service = call.scope.get<S3Service>()

        val request = call.receiveNullable<OrganisationRequest>() ?: return@put call.respond(
            HttpStatusCode.BadRequest, "Data is not correct")

        val organisationRepository = call.scope.get<OrganisationRepository>()

        val organisation = organisationRepository.getById(organisationId, "Not found organisation with this id")

        if (request.logoUrl.isNullOrEmpty() || request.logoUrl != organisation.logoUrl) {
            organisation.logoUrl?.let {  s3Service.deleteFile(it) }
        }

        if (organisationRepository.updateOrganisation(organisationId, request)) {
            return@put call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.deleteOrganisation() {
    delete {
        val organisationId =
            call.parameters["organisationId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing ID")

        val organisationRepository = call.scope.get<OrganisationRepository>()

        if (organisationRepository.deleteById(organisationId)) {
            return@delete call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound, "You don't have permission to deleteOne this organisation")
        }
    }
}