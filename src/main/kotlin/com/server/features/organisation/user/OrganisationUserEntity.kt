package com.server.features.organisation.user;

import com.server.features.organisation.OrganisationRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class OrganisationUserEntity(
    @BsonId
    val id: ObjectId = ObjectId(),
    @BsonProperty("organisation_user_name")
    val organisationUserName: String,
    @BsonProperty("organisation_id")
    val organisationId: String,
    @BsonProperty("organisation_role")
    val organisationRole: OrganisationRole,
    @BsonProperty("organisation_user_status")
    val organisationUserStatus: OrganisationUserStatus,
    @BsonProperty("user_id")
    val userId: String,
)

@Serializable
data class OrganisationUserView(
    val id: String,
    @SerialName("organisation_user_name")
    val organisationUserName: String,
    @SerialName("organisation_name")
    val organisationName: String,
    @SerialName("organisation_role_name")
    val organisationRole: OrganisationRole,
    @SerialName("organisation_user_status")
    val organisationUserStatus: OrganisationUserStatus
)

@Serializable
data class OrganisationUserDto(
    val id: String,
    @SerialName("organisation_user_name")
    val organisationUserName: String,
    @SerialName("organisation_role")
    val organisationRole: OrganisationRole,
    @SerialName("user_id")
    val userId: String? = null,
    val email: String? = null,
    @SerialName("organisation_user_status")
    val organisationUserStatus: OrganisationUserStatus
)


fun OrganisationUserEntity.mapToDto() = OrganisationUserDto(
    id = this.id.toHexString(),
    organisationUserName = this.organisationUserName,
    organisationRole = this.organisationRole,
    organisationUserStatus = this.organisationUserStatus,
    email = null,
    userId = this.userId,
)

