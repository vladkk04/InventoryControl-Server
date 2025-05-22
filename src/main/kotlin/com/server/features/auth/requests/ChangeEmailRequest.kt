package com.server.features.auth.requests

import kotlinx.serialization.Serializable

@Serializable
data class ChangeEmailRequest(
    val email: String
)