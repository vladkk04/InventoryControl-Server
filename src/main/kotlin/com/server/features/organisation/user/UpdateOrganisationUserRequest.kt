package com.server.features.organisation.user

import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrganisationUserRequest(
    val name: String
)
