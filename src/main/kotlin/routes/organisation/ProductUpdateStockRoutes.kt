package routes.organisation

import com.server.extractPrincipalUserId
import com.server.features.organisation.product.ProductRepository
import com.server.features.updateStockProduct.*
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope

fun Route.productUpdateStockRoutes() {
    updateProductStockRoute()
    getAllByOrganisation()
}

fun Route.getAllProductUpdateStock() {
    get("/update-stock") {
        val updateStockProductRepository = call.scope.get<UpdateStockProductRepository>()
        val productId = call.parameters["productId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        val updateStocks = updateStockProductRepository.getAllByProductId(productId)

        return@get call.respond(HttpStatusCode.OK, updateStocks)
    }
}

fun Route.getAllByOrganisation() {
    get("update-stock") {
        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        val updateStockProductRepository = call.scope.get<UpdateStockProductRepository>()

        val result = updateStockProductRepository.getAllByOrganisationId(organisationId).map { it.mapToDto() }

        return@get call.respond(HttpStatusCode.OK, result)

    }
    get("update-stock-view") {
        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        val updateStockProductRepository = call.scope.get<UpdateStockProductRepository>()

        val result = updateStockProductRepository.getAllByOrganisationIdView(organisationId)

        return@get call.respond(HttpStatusCode.OK, result)
    }
}

private fun Route.updateProductStockRoute() {
    post ("/update-stock") {
        val organisationId = call.parameters["organisationId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
        val updateStockProductRepository = call.scope.get<UpdateStockProductRepository>()
        val productRepository = call.scope.get<ProductRepository>()

        val request = call.receiveNullable<UpdateStockProductRequest>() ?: return@post call.respond(HttpStatusCode.Conflict)


        val newProductUpdateStock = ProductUpdateStock(
            organisationId = organisationId,
            productsUpdates = request.productsUpdates,
            updatedBy = userId,
            orderId = request.orderId
        )

        if(updateStockProductRepository.insert(newProductUpdateStock, "Something went wrong")) {

            productRepository.updateProductStock(newProductUpdateStock.productsUpdates)

            return@post call.respond(HttpStatusCode.OK, newProductUpdateStock.mapToDto())
        }

    }
}