package routes.organisation

import com.server.extractPrincipalUserId
import com.server.features.organisation.order.OrderRepository
import com.server.features.organisation.product.*
import com.server.features.s3.S3Service
import com.server.features.updateStockProduct.ProductStockUpdate
import com.server.features.updateStockProduct.ProductUpdateStock
import com.server.features.updateStockProduct.UpdateStockProductRepository
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.scope
import com.server.plugins.ProductValidationPlugin
import java.time.Instant
import java.util.*

fun Route.productRoutes() {
    route("/products") {
        createProduct()
        getAllProducts()

        productUpdateStockRoutes()

        route("/{productId}") {
            install(ProductValidationPlugin)
            getProduct()
            getAllProductUpdateStock()
            updateProduct()
            deleteProduct()
        }
    }
}

private fun Route.getProduct() {
    get {
        val productRepository = call.scope.get<ProductRepository>()
        val productId = call.parameters["productId"] ?: return@get call.respond(HttpStatusCode.BadRequest)


        call.respond(
            HttpStatusCode.OK,
            productRepository.getById(productId, "Not found product with this id").mapToDto()
        )
    }
}

private fun Route.getAllProducts() {
    get {
        val productRepository = call.scope.get<ProductRepository>()

        val organisationId = call.parameters["organisationId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

        call.respond(
            HttpStatusCode.OK,
            productRepository.getAllByOrganisationId(organisationId).map { it.mapToDto() }
        )

    }
}


private fun Route.createProduct() {
    post("/create") {

        val productRepository = call.scope.get<ProductRepository>()
        val updateStockProductRepository = call.scope.get<UpdateStockProductRepository>()

        val request = call.receiveNullable<ProductRequest>() ?: return@post call.respond(
            HttpStatusCode.OK,
            "raw"
        )

        val userId = call.extractPrincipalUserId() ?: return@post call.respond(HttpStatusCode.Unauthorized)
        val organisationId = call.parameters["organisationId"] ?: return@post call.respond(HttpStatusCode.BadRequest)

        val newProduct = Product(
            imageUrl = request.imageUrl,
            name = request.name,
            barcode = request.barcode,
            quantity = request.quantity,
            unit = request.unit,
            minStockLevel = request.minStockLevel,
            description = request.description,
            categoryId = request.categoryId,
            organisationId = organisationId,
            tags = request.tags,
            updates = emptyList(),
            createdBy = userId
        )

        updateStockProductRepository.insert(
            ProductUpdateStock(
                organisationId = organisationId,
                productsUpdates = listOf(
                    ProductStockUpdate(
                        newProduct.id.toHexString(),
                        previousStock = 0.00,
                        adjustmentValue = request.quantity
                    )
                ),
                updatedBy = userId
            ),
            "Something went wrong"
        )


        if (productRepository.insert(newProduct, "Product already exist")) {
            return@post call.respond(HttpStatusCode.OK, newProduct.mapToDto())
        }
        return@post call.respond(HttpStatusCode.OK, "Something went wrong")
    }
}

private fun Route.updateProduct() {
    put {
        val productRepository = call.scope.get<ProductRepository>()
        val s3Service = call.scope.get<S3Service>()

        val request = call.receiveNullable<ProductRequest>() ?: return@put call.respond(HttpStatusCode.OK, "raw")

        val userId = call.extractPrincipalUserId() ?: return@put call.respond(HttpStatusCode.Unauthorized)
        val productId = call.parameters["productId"] ?: return@put call.respond(HttpStatusCode.BadRequest)

        if (request.imageUrl.isNullOrEmpty()) {
            val product = productRepository.getById(productId, errorMessage = "Product not found")
            product.imageUrl?.let { s3Service.deleteFile(it) }
        }

        val update = ProductUpdate(
            imageUrl = request.imageUrl,
            name = request.name,
            barcode = request.barcode,
            quantity = request.quantity,
            unit = request.unit,
            minStockLevel = request.minStockLevel,
            description = request.description,
            categoryId = request.categoryId,
            tags = request.tags,
            update = ProductUpdateHistory(
                updatedAt = Date.from(Instant.now()).time,
                updatedBy = userId
            )
        )

        if (productRepository.updateById(productId, update)) {
            return@put call.respond(HttpStatusCode.OK, productRepository.getById(productId, "Product not found").mapToDto())
        }
    }
}

private fun Route.deleteProduct() {
    delete {
        val productRepository = call.scope.get<ProductRepository>()
        val orderRepository = call.scope.get<OrderRepository>()
        val productUpdate = call.scope.get<UpdateStockProductRepository>()
        val s3Service = call.scope.get<S3Service>()
        val productId = call.parameters["productId"] ?: return@delete
        val product = productRepository.getById(productId, "Not found product with this id")
        val organisationId = call.parameters["organisationId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

        if (!productUpdate.deleteProductInUpdatedStock(organisationId, productId)) {
            return@delete call.respond(HttpStatusCode.BadRequest)
        }

        product.imageUrl?.let { s3Service.deleteFile(it) }

        if (productRepository.deleteById(productId)) {
            orderRepository.deleteProduct(productId)
            return@delete call.respond(HttpStatusCode.OK, "Deleted")
        }

        return@delete call.respond(HttpStatusCode.BadRequest, "Something went wrong")

    }
}