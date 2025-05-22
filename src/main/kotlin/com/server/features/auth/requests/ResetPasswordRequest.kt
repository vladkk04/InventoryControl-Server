package com.server.features.auth.requests

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val password: String,
    val token: String
)
