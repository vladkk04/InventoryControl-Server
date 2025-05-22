package com.server.features.updateStockProduct;

import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.`in`
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.server.features.organisation.product.ProductRepository

class UpdateStockProductRepositoryImpl(
    override val db: MongoDatabase,
    private val productRepository: ProductRepository
) : UpdateStockProductRepository() {

    override val collection: MongoCollection<ProductUpdateStock>
        get() = db.getCollection("updates_product_stock")

    override suspend fun deleteByOrderId(id: String): Boolean {
        return deleteOne(eq("order_id", id), DeleteOptions())
    }

    override suspend fun getByOrderId(id: String): ProductUpdateStock {
        return getBy(eq("order_id", id), "Order not found with this id")
    }

    override suspend fun deleteProductInUpdatedStock(organisationId: String, productId: String): Boolean {
        val pullUpdate = Updates.pull("products", eq("product_id", productId))

        val updatedDocument = collection.updateMany(
            eq("organisation_id", organisationId),
            pullUpdate,
            UpdateOptions()
        ).wasAcknowledged()

        val deleteResult = collection.deleteMany(
            Filters.and(
                Filters.size("products", 0),
                Filters.exists("products", true)
            )
        ).wasAcknowledged()


        return updatedDocument && deleteResult
    }

    override suspend fun getAllByProductId(productId: String): List<ChangeStockProduct> {
        return getAll(
            `in`("products.product_id", productId),
        ).flatMap { product ->
            product.productsUpdates.filter { it.productId == productId }
                .map {
                    ChangeStockProduct(
                        it.productId,
                        it.previousStock,
                        it.adjustmentValue,
                        product.updatedBy,
                        product.id.date.time
                    )
                }
        }
    }

    override suspend fun getAllByOrganisationIdView(organisationId: String): List<ProductUpdateStockViewDto> {
        val stockUpdates = getAll(eq("organisation_id", organisationId))

        if (stockUpdates.isEmpty()) return emptyList()

        val productIds = stockUpdates.flatMap { update ->
            update.productsUpdates.map { it.productId }
        }.distinct()

        val products = productRepository.getAllIn(productIds)
            .associateBy { it.id.toHexString() }

        val result = stockUpdates.map { update ->
            ProductUpdateStockViewDto(
                id = update.id.toHexString(),
                products = update.productsUpdates.mapNotNull { stockItem ->
                    products[stockItem.productId]?.let { product ->
                        ProductWithDetails(
                            productId = stockItem.productId,
                            name = product.name,
                            unit = product.unit,
                            previousStock = stockItem.previousStock,
                            adjustmentValue = stockItem.adjustmentValue
                        )
                    }
                },
                updatedBy = update.updatedBy,
                updatedAt = update.id.date.time
            )
        }

        return result
    }


    override suspend fun getAllByOrganisationId(organisationId: String): List<ProductUpdateStock> {
        return getAll(eq("organisation_id", organisationId))
    }
}
