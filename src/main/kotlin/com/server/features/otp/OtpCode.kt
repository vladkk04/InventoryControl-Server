package com.server.features.otp

import org.bson.codecs.pojo.annotations.BsonProperty
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

data class OtpCode(
    val email: String,
    val otp: String,
    @BsonProperty("expire_at")
    val expireAt: Date
)
