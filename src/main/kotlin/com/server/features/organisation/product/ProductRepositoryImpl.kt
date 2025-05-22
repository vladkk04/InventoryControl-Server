package com.server.features.organisation.product;

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.UpdateManyModel
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates.*
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.server.features.organisation.productCategory.ProductCategory
import com.server.features.s3.S3Service
import com.server.features.updateStockProduct.ProductStockUpdate
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId

class ProductRepositoryImpl(
    override val db: MongoDatabase,
) : ProductRepository() {

    override val collection: MongoCollection<Product>
        get() = db.getCollection<Product>("products").apply {
            runBlocking {
                createIndex(
                    Indexes.compoundIndex(
                        Indexes.ascending("organisation_id"),
                        Indexes.ascending(Product::name.name)
                    ),
                    IndexOptions().unique(true)
                )
            }
        }


    override suspend fun getAllByOrganisationId(organisationId: String): List<Product> {
        return getAll(eq("organisation_id", organisationId))
    }

    override suspend fun getAllIn(listIds: List<String>): List<Product> {
        return getAll(`in`("_id", listIds.map { ObjectId(it) }))
    }

    override suspend fun updateProductStock(updatedProductStock: List<ProductStockUpdate>) {
        val bulkOperations = updatedProductStock.map { update ->
            UpdateManyModel<Product>(
                eq("_id", ObjectId(update.productId)),
                combine(
                    set(Product::quantity.name, update.previousStock + update.adjustmentValue)
                )
            )
        }

        collection.bulkWrite(bulkOperations)

    }

    override suspend fun updateById(id: String, update: ProductUpdate): Boolean {
        return updateById(
            id,
            combine(
                set("image_url", update.imageUrl),
                set(Product::name.name, update.name),
                set(Product::barcode.name, update.barcode),
                set(Product::quantity.name, update.quantity),
                set(Product::unit.name, update.unit),
                set("category_id", update.categoryId),
                set("min_stock_level", update.minStockLevel),
                set(Product::description.name, update.description),
                set(Product::tags.name, update.tags),
                push(Product::updates.name, update.update),
            ),
            UpdateOptions()
        )
    }
}