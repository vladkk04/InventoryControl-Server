package routes.organisation

import com.server.features.organisation.settings.OrganisationSettings
import com.server.features.organisation.settings.OrganisationSettingsRepository
import com.server.features.organisation.settings.OrganisationSettingsRequest
import com.server.features.organisation.settings.mapToDto
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope


fun Route.organisationSettingsRoutes() {
    route("/organisation-settings") {
        //createOrganisationSettingsRoute()
        updateOrganisationSettingsRoute()
        getOrganisationSettingsRoute()
    }
}

private fun Route.createOrganisationSettingsRoute() {
    post {
        val repository = call.scope.get<OrganisationSettingsRepository>()
        val organisationId = call.parameters["organisationId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val request =
            call.receiveNullable<OrganisationSettingsRequest>() ?: return@post call.respond(HttpStatusCode.BadRequest)

        val newSettings = OrganisationSettings(
            notificationSettings = request.notificationSettings,
            thresholdSettings = request.thresholdSettings,
            organisationId = organisationId
        )

        if (repository.insert(newSettings, "Something went wrong")) {
            return@post call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.updateOrganisationSettingsRoute() {
    put {
        val repository = call.scope.get<OrganisationSettingsRepository>()
        val request = call.receiveNullable<OrganisationSettingsRequest>() ?: return@put call.respond(HttpStatusCode.BadRequest)
        val organisationId = call.parameters["organisationId"] ?: return@put call.respond(HttpStatusCode.BadRequest)

        if (repository.updateByOrganisationId(organisationId, request)) {
            return@put call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.getOrganisationSettingsRoute() {
    get {
        val repository = call.scope.get<OrganisationSettingsRepository>()
        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)


        return@get call.respond(repository.getByOrganisationId(organisationId).mapToDto())
    }

}
