package com.server.features.otp

import io.ktor.util.*

interface EmailOtpRepository {

    suspend fun generateOneTimeOtp(email: String): String

    suspend fun validateOneTimeOtp(email: String, otp: String): Boolean

}