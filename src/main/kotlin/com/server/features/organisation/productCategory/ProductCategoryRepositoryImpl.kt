package com.server.features.organisation.productCategory;

import com.mongodb.client.model.*
import com.mongodb.client.model.Updates.set
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking

class ProductCategoryRepositoryImpl(
    override val db: MongoDatabase,
) : ProductCategoryRepository() {

    override val collection: MongoCollection<ProductCategory>
        get() = db.getCollection<ProductCategory>("product_categories").apply {
            runBlocking {
                createIndex(
                    Indexes.compoundIndex(
                        Indexes.ascending("organisation_id"),
                        Indexes.ascending(ProductCategory::name.name)
                    ),
                    IndexOptions().unique(true))
            }
        }


    override suspend fun getAllByOrganisationId(organisationId: String): List<ProductCategory> =
        getAll(eq("organisation_id", organisationId))

    override suspend fun deleteByOrganisationId(organisationId: String) =
        deleteOne(eq("organisation_id", organisationId), DeleteOptions())

    override suspend fun updateById(id: String, update: ProductCategoryRequest): Boolean =
        updateById(
            id = id,
            update = set(ProductCategoryRequest::name.name, update.name),
            updateOptions = UpdateOptions()
        )

}
