package com.server.features.organisation.invite.requests;

import com.server.features.organisation.OrganisationRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganisationInvitationUserIdRequest(
    @SerialName("organisation_user_name")
    val organisationUserName: String,
    @SerialName("organisation_role")
    val organisationRole: OrganisationRole,
    @SerialName("user_id")
    val userId: String,
)