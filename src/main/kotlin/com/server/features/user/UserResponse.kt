package com.server.features.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

@Serializable
data class UserResponse(
    val id: String,
    @SerialName("full_name")
    val fullName: String,
    val email: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("registered_at")
    val registeredAt: Int,
)
