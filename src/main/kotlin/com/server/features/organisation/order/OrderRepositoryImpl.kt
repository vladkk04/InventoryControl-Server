package com.server.features.organisation.order;

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.elemMatch
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Updates.*
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.server.features.s3.S3Service
import kotlinx.coroutines.flow.toList

class OrderRepositoryImpl(
    override val db: MongoDatabase,
    private val s3Service: S3Service
) : OrderRepository() {

    override val collection: MongoCollection<Order>
        get() = db.getCollection("orders")

    override suspend fun getAllByOrganisationId(organisationId: String): List<Order> =
        getAll(eq("organisation_id", organisationId))

    override suspend fun deleteProduct(productId: String): Boolean {
        // 1. First find all orders containing this product to get file references
        val ordersWithProduct = collection.find(
            elemMatch("products", eq("product_id", productId))
        ).toList()

        val filesToDelete = ordersWithProduct.flatMap { order ->
           order.attachments
        }.distinct()

        val pullResult = collection.updateMany(
            elemMatch("products", eq("product_id", productId)),
            pull("products", eq("product_id", productId))
        ).wasAcknowledged()

        val deleteOrdersResult = collection.deleteMany(
            Filters.and(
                Filters.size("products", 0),
                Filters.exists("products", true)
            )
        ).wasAcknowledged()

        if(filesToDelete.isNotEmpty()) {
            s3Service.deleteFiles(filesToDelete.map { it.url }.toSet())
        }

        return pullResult && deleteOrdersResult /*&& result == true*/
    }

    override suspend fun updateById(id: String, update: OrderRequest): Boolean =
        updateById(
            id = id,
            update = combine(
                set(Order::products.name, update.products),
                set(Order::discount.name, update.discount),
                set(Order::comment.name, update.comment),
                set(Order::attachments.name, update.attachments),
            ),
            updateOptions = UpdateOptions()
        )


}