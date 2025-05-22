package com.server.features.organisation

import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Filters.`in`
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId

class OrganisationRepositoryImpl(
    override val db: MongoDatabase,
) : OrganisationRepository() {

    override val collection: MongoCollection<Organisation> = db.getCollection<Organisation>("organisations").apply {
        runBlocking {
            this@apply.createIndex(Indexes.ascending(Organisation::name.name), IndexOptions().unique(true))
        }
    }

    override suspend fun updateOrganisation(id: String, update: OrganisationRequest): Boolean {
        return updateById(
            id = id,
            update = combine(
                set("logo_url", update.logoUrl),
                set(Organisation::name.name, update.name),
                set(Organisation::currency.name, update.currency),
                set(Organisation::description.name, update.description),
            ),
            updateOptions = UpdateOptions()
        )
    }

    override suspend fun getAllByIds(ids: Set<String>): List<Organisation> {
        return getAll(`in`("_id", ids.map { ObjectId(it) }))
    }

    override suspend fun getAllOrganisationsByUserId(userId: String): List<Organisation> =
        getAll(eq("created_by", userId))


    override suspend fun deleteById(id: String): Boolean {
        return deleteOne(
            filter = eq("_id", ObjectId(id)),
            options = DeleteOptions()
        )
    }

}
