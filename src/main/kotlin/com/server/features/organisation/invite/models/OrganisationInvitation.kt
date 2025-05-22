package com.server.features.organisation.invite.models;

import com.server.features.organisation.OrganisationRole
import com.server.features.organisation.user.OrganisationUserDto
import com.server.features.organisation.user.OrganisationUserStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


data class OrganisationInvitationEntity(
    @BsonId
    val id: ObjectId = ObjectId(),
    @BsonProperty("organisation_user_name")
    val organisationUserName: String,
    @BsonProperty("organisation_id")
    val organisationId: String,
    @BsonProperty("user_id")
    val userId: String? = null,
    val email: String? = null,
    @BsonProperty("organisation_role")
    val organisationRole: OrganisationRole,
    val status: StatusInvitation = StatusInvitation.PENDING,
    @BsonProperty("invited_by")
    val invitedBy: String,
    @BsonProperty("invited_at")
    val invitedAt:Date = Date.from(Instant.now()),
    @BsonProperty("expire_at")
    val expireAt: Date = Date.from(Instant.now().plus(7, ChronoUnit.DAYS)),
)

@Serializable
data class OrganisationInvitationDto(
    @SerialName("id")
    val id: String,
    @SerialName("organisation_user_name")
    val organisationUserName: String,
    @SerialName("organisation_id")
    val organisationId: String,
    @SerialName("organisation_role")
    val organisationRole: OrganisationRole,
    @SerialName("user_id")
    val userId: String? = null,
    val email: String? = null,
    val status: StatusInvitation = StatusInvitation.PENDING,
    @SerialName("invited_by")
    val invitedBy: String,
    @SerialName("invited_at")
    val invitedAt: Int,
    @SerialName("expire_at")
    val expireAt: Long,
)


@Serializable
data class OrganisationInvitationViewDto(
    val id: String,
    @SerialName("organisation_name")
    val organisationName: String,
    @SerialName("organisation_role")
    val organisationRole: OrganisationRole,
    val status: StatusInvitation,
    @SerialName("invited_by")
    val invitedBy: String,
    @SerialName("expire_at")
    val expireAt: Long

)


fun OrganisationInvitationEntity.mapToDto() =
    OrganisationInvitationDto(
        id = this.id.toHexString(),
        organisationUserName = this.organisationUserName,
        organisationId = this.organisationId,
        organisationRole = this.organisationRole,
        userId = this.userId,
        email = this.email,
        status = this.status,
        invitedBy = this.invitedBy,
        invitedAt = id.timestamp,
        expireAt = expireAt.time,
    )

fun OrganisationInvitationEntity.mapToOrganisationUserDto() =
    OrganisationUserDto(
        id = this.id.toHexString(),
        organisationUserName = this.organisationUserName,
        organisationRole = this.organisationRole,
        userId = this.userId,
        email = this.email,
        organisationUserStatus = when (this.status) {
            StatusInvitation.PENDING -> OrganisationUserStatus.PENDING
            StatusInvitation.ACCEPTED -> OrganisationUserStatus.ACTIVE
            StatusInvitation.DECLINED -> OrganisationUserStatus.DECLINED
        }
    )
