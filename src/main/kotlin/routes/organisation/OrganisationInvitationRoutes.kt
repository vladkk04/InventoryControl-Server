package routes.organisation

import com.server.features.organisation.invite.OrganisationInvitationRepository
import com.server.features.organisation.invite.models.mapToDto
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope

fun Route.organisationInvitationRoutes() {
    route("/invitations") {
        getAllOrganisationsInvitationsRoutes()
    }
}

private fun Route.getAllOrganisationsInvitationsRoutes() {
    get {
        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val organisationInvitationRepository = call.scope.get<OrganisationInvitationRepository>()

        return@get call.respond(HttpStatusCode.OK, organisationInvitationRepository.getAllByOrganisationId(organisationId).map { it.mapToDto() })

    }
}