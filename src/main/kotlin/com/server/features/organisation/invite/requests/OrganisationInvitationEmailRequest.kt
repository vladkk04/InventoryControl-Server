package com.server.features.organisation.invite.requests;

import com.server.features.organisation.OrganisationRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrganisationInvitationEmailRequest(
    @SerialName("organisation_user_name")
    val organisationUserName: String,
    @SerialName("organisation_role")
    val organisationRole: OrganisationRole,
    val email: String,
)