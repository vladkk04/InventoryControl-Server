package com.server.features.organisation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

data class Organisation(
    @BsonId
    val id: ObjectId = ObjectId(),
    val name: String,
    val currency: String,
    val description: String,
    @BsonProperty("logo_url")
    val logoUrl: String? = null,
    @BsonProperty("created_by")
    val createdBy: String,
)

@Serializable
data class OrganisationDto(
    val id: String,
    val name: String,
    val currency: String,
    val description: String,
    @SerialName("logo_url")
    val logoUrl: String? = null,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("created_at")
    val createdAt: Long,
)

fun Organisation.mapToDto() = OrganisationDto(
    id = this.id.toHexString(),
    name = this.name,
    currency = this.currency,
    description = this.description,
    logoUrl = this.logoUrl,
    createdBy = this.createdBy,
    createdAt = this.id.date.time
)
