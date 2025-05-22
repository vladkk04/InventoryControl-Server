package com.server.features.auth.responses

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val token: String
)
