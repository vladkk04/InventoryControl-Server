package com.server.features.email

import org.bson.codecs.pojo.annotations.BsonProperty
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

data class EmailToken(
    val email: String,
    val token: String,
    val createdAt: Date = Date.from(Instant.now()),
    @BsonProperty("expire_at")
    val expireAt: Date = Date.from(Instant.now().plus(7, ChronoUnit.DAYS)),
)
