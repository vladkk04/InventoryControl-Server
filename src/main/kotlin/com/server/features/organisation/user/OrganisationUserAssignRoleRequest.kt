package com.server.features.organisation.user

import com.server.features.organisation.OrganisationRole
import kotlinx.serialization.Serializable

@Serializable
data class OrganisationUserAssignRoleRequest(
    val role: OrganisationRole
)
