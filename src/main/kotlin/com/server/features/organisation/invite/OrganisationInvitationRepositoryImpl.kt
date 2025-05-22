package com.server.features.organisation.invite;

import com.mongodb.client.model.*
import com.mongodb.client.model.Aggregates.*
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Projections.*
import com.mongodb.client.model.Updates.*
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.server.features.common.safeAggregate
import com.server.features.organisation.invite.models.OrganisationInvitationEntity
import com.server.features.organisation.invite.models.OrganisationInvitationViewDto
import com.server.features.organisation.invite.models.StatusInvitation
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.bson.types.ObjectId
import java.util.concurrent.TimeUnit

class OrganisationInvitationRepositoryImpl(
    override val db: MongoDatabase,
) : OrganisationInvitationRepository() {

    override val collection: MongoCollection<OrganisationInvitationEntity> =
        db.getCollection<OrganisationInvitationEntity>("organisation_invitations").apply {
            runBlocking {
                try {
                    this@apply.createIndex(
                        Indexes.ascending("expire_at"),
                        IndexOptions().expireAfter(0, TimeUnit.SECONDS)
                    )
                    this@apply.createIndex(
                        Indexes.ascending("invited_at"),
                        IndexOptions()
                            .expireAfter(0, TimeUnit.SECONDS)
                            .partialFilterExpression(
                                eq("status", "ACCEPTED")
                            )
                    )
                } catch (e: Exception) {
                    println("Failed to create indexes: ${e.message}")
                }
            }
        }

    override suspend fun getAllView(email: String?, userId: String?): List<OrganisationInvitationViewDto> {

        val aggregationPipeline = listOf(
            match(
                or(
                    eq("email", email),
                    eq("user_id", userId)
                )
            ),

            addFields(
                Field("organisationObjectId", Document("\$toObjectId", "\$organisation_id")),
                Field("inviterObjectId", Document("\$toObjectId", "\$invited_by")),
            ),
            lookup(
                "organisations",
                "organisationObjectId",
                "_id",
                "organisation"
            ),
            lookup(
                "users",
                "inviterObjectId",
                "_id",
                "inviter"
            ),
            unwind("\$organisation"),
            unwind("\$inviter"),
            project(
                fields(
                    computed("id", mapOf("\$toString" to "\$_id")), // Convert ObjectId to String
                    computed("organisation_name", "\$organisation.name"),
                    computed("organisation_role", "\$organisation_role"),
                    computed("status", "\$status"),
                    computed("invited_by", "\$inviter.full_name"),
                    computed("expire_at", mapOf("\$toLong" to "\$expire_at"))
                )
            )
        )

        return collection.safeAggregate<OrganisationInvitationViewDto>(aggregationPipeline).toList()
    }


    override suspend fun getAllByOrganisationId(organisationId: String): List<OrganisationInvitationEntity> {
        return getAll(eq("organisation_id", organisationId))
    }

    override suspend fun inviteAgain(entity: OrganisationInvitationEntity): Boolean {
        return updateOne(
            and(
                eq("email", entity.email),
                eq("user_id", entity.userId)
            ),
            combine(
                set("email", entity.email),
                set("user_id", entity.userId),
                set("organisation_user_name", entity.organisationUserName),
                set("organisation_role", entity.organisationRole),
                set("organisation_id", entity.organisationId),
                set("invited_by", entity.invitedBy),
                set("invited_at", entity.invitedAt),
                set("expire_at", entity.expireAt),
                set("status", StatusInvitation.PENDING)
            ),
            UpdateOptions().upsert(true)
        )
    }


    override suspend fun updateInvitationStatus(invitationId: String, status: StatusInvitation): Boolean {
        return updateOne(
            filter = eq("_id", ObjectId(invitationId)),
            update = set(OrganisationInvitationEntity::status.name, status),
            updateOptions = UpdateOptions()
        )
    }

    override suspend fun deleteByUserId(userId: String): Boolean {
        return deleteOne(eq("user_id", userId), DeleteOptions())
    }

    override suspend fun deleteByEmail(email: String): Boolean {
        return deleteOne(eq("email", email), DeleteOptions())
    }


}