package com.server.features.organisation.user;

import com.mongodb.client.model.*
import com.mongodb.client.model.Aggregates.*
import com.mongodb.client.model.Filters.and
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.*
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.server.features.common.safeAggregate
import com.server.features.organisation.OrganisationRole
import com.server.features.organisation.invite.OrganisationInvitationRepository
import com.server.features.organisation.invite.models.mapToOrganisationUserDto
import com.server.projectWithIdConversion
import org.bson.Document

class OrganisationUserRepositoryImpl(
    override val db: MongoDatabase,
    private val organisationInvitationRepository: OrganisationInvitationRepository,
) : OrganisationUserRepository() {

    override val collection: MongoCollection<OrganisationUserEntity>
        get() = db.getCollection("organisation_users")

    override suspend fun getAllByUserId(userId: String): List<OrganisationUserEntity> =
        getAll(eq("user_id", userId))


    override suspend fun getAllViewByUserId(userId: String): List<OrganisationUserView> {
        val aggregationPipeline = listOf(
            addFields(
                Field("orgIdObject", Document("\$toObjectId", "\$organisation_id")),
                Field("roleIdObject", Document("\$toObjectId", "\$organisation_role_id")),
                Field("userIdObject", Document("\$toObjectId", "\$invited_by"))
            ),
            lookup(
                "organisations",
                "orgIdObject",
                "_id",
                "organisation"
            ),
            lookup(
                "organisation_roles",
                "roleIdObject",
                "_id",
                "role"
            ),
            lookup(
                "users",
                "userIdObject",
                "_id",
                "user"
            ),
            unwind("\$organisation", UnwindOptions().preserveNullAndEmptyArrays(false)),
            unwind("\$role", UnwindOptions().preserveNullAndEmptyArrays(false)),
            unwind("\$user", UnwindOptions().preserveNullAndEmptyArrays(false)),
            projectWithIdConversion(
                "id" to "_id",
                "organisation_name" to "organisation.name",
                "organisation_role_name" to "role.name",
                "invited_by" to "user.full_name",
                "expire_at" to mapOf("\$toLong" to "\$expire_at")
            )
        )

        return collection.safeAggregate<OrganisationUserView>(aggregationPipeline).toList()
    }

    override suspend fun cancelInviteByUserId(userId: String): Boolean {
        return organisationInvitationRepository.deleteByUserId(userId)
    }

    override suspend fun cancelInviteByEmail(email: String): Boolean {
        return organisationInvitationRepository.deleteByEmail(email)
    }

    override suspend fun makeUserInactive(organisationUserid: String): Boolean {
        return updateById(
            organisationUserid,
            set("organisation_user_status", OrganisationUserStatus.INACTIVE),
            UpdateOptions()
        )
    }

    override suspend fun makeUserActive(organisationUserid: String): Boolean {
        return updateById(
            organisationUserid,
            set("organisation_user_status", OrganisationUserStatus.ACTIVE),
            UpdateOptions()
        )
    }

    override suspend fun getAllPendingAndAccepted(): List<OrganisationUserView> {
        /*val aggregationPipeline = listOf(
            addFields(
                Field("orgIdObject", Document("\$toObjectId", "\$organisation_id")),
                Field("roleIdObject", Document("\$toObjectId", "\$organisation_role_id")),
                Field("userIdObject", Document("\$toObjectId", "\$invited_by"))
            ),
            lookup(
                "organisations",
                "orgIdObject",
                "_id",
                "organisation"
            ),
            lookup(
                "organisation_roles",
                "roleIdObject",
                "_id",
                "role"
            ),
            lookup(
                "users",
                "userIdObject",
                "_id",
                "user"
            ),
            unwind("\$organisation", UnwindOptions().preserveNullAndEmptyArrays(false)),
            unwind("\$role", UnwindOptions().preserveNullAndEmptyArrays(false)),
            unwind("\$user", UnwindOptions().preserveNullAndEmptyArrays(false)),
            projectWithIdConversion(
                "id" to "_id",
                "organisation_name" to "organisation.name",
                "organisation_role_name" to "role.name",
                "invited_by" to "user.full_name",
                "expire_at" to mapOf("\$toLong" to "\$expire_at")
            )
        )*/
        return emptyList()
    }

    override suspend fun getByUserId(userId: String, organisationId: String): OrganisationUserEntity {
        return getBy(
            and(
                eq("user_id", userId),
                eq("organisation_id", organisationId)
            ),
            "Organisation user not found with this id"
        )
    }

    override suspend fun assignRoleByUserId(id: String, role: OrganisationRole): Boolean {
        return updateById(
            id,
            set("organisation_role", role),
            updateOptions = UpdateOptions()
        )
    }

    override suspend fun changeOrganisationUserNameByUserId(
        id: String,
        update: UpdateOrganisationUserRequest
    ): Boolean {
        return updateById(
            id,
            set("organisation_user_name", update.name),
            UpdateOptions()
        )
    }

    override suspend fun getAllByOrganisationId(organisationId: String): List<OrganisationUserDto> {
        val organisationUsers = getAll(eq("organisation_id", organisationId)).map { it.mapToDto() }
        val organisationInvitations =
            organisationInvitationRepository.getAllByOrganisationId(organisationId)
                .map { it.mapToOrganisationUserDto() }
                .filter {
                    it.organisationUserStatus == OrganisationUserStatus.PENDING ||
                            it.organisationUserStatus == OrganisationUserStatus.DECLINED
                }


        return organisationUsers + organisationInvitations

    }

}
