package com.server.features.organisation.settings;

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.client.model.Updates.combine
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase


class OrganisationSettingsRepositoryImpl(
    override val db: MongoDatabase,
) : OrganisationSettingsRepository() {


    override val collection: MongoCollection<OrganisationSettings>
        get() = db.getCollection("organisation_settings")

    override suspend fun getByOrganisationId(organisationId: String): OrganisationSettings =
        getBy(eq("organisation_id", organisationId), "Organisation not found with this id")

    override suspend fun updateByOrganisationId(organisationId: String, update: OrganisationSettingsRequest): Boolean =
        updateOne(
            filter = eq("organisation_id", organisationId),
            update = combine(
                Updates.set("threshold_settings", update.thresholdSettings),
                Updates.set("notification_settings", update.notificationSettings)
            ),
            updateOptions = UpdateOptions().upsert(true)
        )



}