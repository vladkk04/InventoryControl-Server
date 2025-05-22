package com.server.features.auth.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticateResponse(
    @SerialName("user_id")
    val userId: String
)
