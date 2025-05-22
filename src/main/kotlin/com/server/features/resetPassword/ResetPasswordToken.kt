package com.server.features.resetPassword

import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

data class ResetPasswordToken(
    val token: String,
    val email: String,
    val createdAt: Date = Date.from(Instant.now()),
    @BsonProperty("expire_at")
    val expireAt: Date = Date.from(Instant.now().plus(15, ChronoUnit.MINUTES)),
)
