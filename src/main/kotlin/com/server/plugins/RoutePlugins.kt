package com.server.plugins

import com.server.features.organisation.OrganisationRepository
import com.server.features.organisation.product.ProductRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import org.koin.ktor.plugin.scope

val OrganisationValidationPlugin = createRouteScopedPlugin(name = "OrganisationValidationPlugin") {
    onCall { call ->
        val organisationRepository = call.scope.get<OrganisationRepository>()
        val id = call.parameters["organisationId"] ?: return@onCall call.respond(HttpStatusCode.BadRequest, "Missing organisation id")

        organisationRepository.getById(id, "Organisation not found with this id")
    }
}

val ProductValidationPlugin = createRouteScopedPlugin(name = "ProductValidationPlugin") {
    onCall { call ->
        val id =
            call.parameters["productId"] ?: return@onCall call.respond(HttpStatusCode.BadRequest, "Missing product id")
        val productRepository = call.scope.get<ProductRepository>()

        productRepository.getById(id, "Product not found with this id")
    }
}
