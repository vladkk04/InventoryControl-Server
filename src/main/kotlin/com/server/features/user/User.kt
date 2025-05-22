package com.server.features.user

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.Instant
import java.util.Date


data class User(
    @BsonId
    val id: ObjectId = ObjectId(),
    @BsonProperty("full_name")
    val fullName: String,
    val email: String,
    @BsonProperty("image_url")
    val imageUrl: String? = null,
    val password: String,
    val salt: String,
    @BsonProperty("email_confirmed_at")
    val emailConfirmedAt: Date? = null,
)
