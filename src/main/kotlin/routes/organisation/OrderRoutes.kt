package routes.organisation

import com.server.extractPrincipalUserId
import com.server.features.organisation.order.Order
import com.server.features.organisation.order.OrderRepository
import com.server.features.organisation.order.OrderRequest
import com.server.features.organisation.order.mapToDto
import com.server.features.organisation.product.ProductRepository
import com.server.features.updateStockProduct.ProductStockUpdate
import com.server.features.updateStockProduct.UpdateStockProductRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope

fun Route.orderRoutes() {
    route("/orders") {
        createOrder()
        getAllOrders()

        route("/{orderId}") {
            getOrder()
            updateOrder()
            deleteOrder()
        }
    }
}

private fun Route.getOrder() {
    get {
        val orderRepository = call.scope.get<OrderRepository>()
        val orderId = call.parameters["orderId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        return@get call.respond(orderRepository.getById(orderId, "Not found order with this id").mapToDto())
    }
}

private fun Route.getAllOrders() {
    get {
        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val orderRepository = call.scope.get<OrderRepository>()

        return@get call.respond(orderRepository.getAllByOrganisationId(organisationId).map { it.mapToDto() })
    }
}


private fun Route.createOrder() {
    post("/create") {
        val organisationId = call.parameters["organisationId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.BadRequest)

        val orderRepository = call.scope.get<OrderRepository>()

        val request = call.receiveNullable<OrderRequest>() ?: return@post call.respond(HttpStatusCode.BadRequest)

        if (request.products.isEmpty()) {
            return@post call.respond(HttpStatusCode.BadRequest, "Must be at least 1 product in order")
        }

        val newOrder = Order(
            products = request.products,
            discount = request.discount,
            comment = request.comment,
            attachments = request.attachments,
            organisationId = organisationId,
            orderedBy = userId
        )

        if (orderRepository.insert(newOrder, "Something went wrong")) {
            return@post call.respond(HttpStatusCode.Created, newOrder.mapToDto())
        }
    }
}

private fun Route.updateOrder() {
    put {
        val orderId = call.parameters["orderId"] ?: return@put call.respond(HttpStatusCode.BadRequest)
        val orderRepository = call.scope.get<OrderRepository>()


        val request = call.receiveNullable<OrderRequest>() ?: return@put call.respond(
            HttpStatusCode.BadRequest
        )

        if (orderRepository.updateById(orderId, request)) {
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.deleteOrder() {
    delete {
        val orderId = call.parameters["orderId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
        val orderRepository = call.scope.get<OrderRepository>()

        val updateStockRepository = call.scope.get<UpdateStockProductRepository>()
        val productRepository = call.scope.get<ProductRepository>()

        val stocks = updateStockRepository.getByOrderId(orderId)

        val productStockUpdates = stocks.productsUpdates.map { productUpdate ->
            ProductStockUpdate(
                productId = productUpdate.productId,
                previousStock = productUpdate.previousStock + productUpdate.adjustmentValue,
                adjustmentValue = -productUpdate.adjustmentValue
            )
        }

        updateStockRepository.deleteByOrderId(orderId)

        productRepository.updateProductStock(productStockUpdates)

        if (orderRepository.deleteById(orderId)) {
            return@delete call.respond(HttpStatusCode.OK)
        } else {
            return@delete call.respond(HttpStatusCode.NotFound)
        }
    }
}