package com.server.features.auth.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OtpRequest(
    val email: String,
    @SerialName("otp_code")
    val otpCode: String
)
