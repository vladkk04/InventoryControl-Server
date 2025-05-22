package com.server.features.auth.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChangePasswordRequest(
    val password: String,
    @SerialName("new_password")
    val newPassword: String
)
