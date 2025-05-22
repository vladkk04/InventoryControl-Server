package com.server.features.auth.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangeInfoUserRequest(
    @SerialName("logo_url")
    val logoUrl: String?,
    @SerialName("full_name")
    val fullName: String
)
