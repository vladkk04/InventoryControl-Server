package com.server.features.organisation.productCategory;

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.Instant
import java.util.*

data class ProductCategory(
    @BsonId
    val id: ObjectId = ObjectId(),
    val name: String,
    @BsonProperty("organisation_id")
    val organisationId: String,
    @BsonProperty("created_by")
    val createdBy: String,
    @BsonProperty("created_at")
    val createdAt: Date = Date.from(Instant.now()),
    @BsonProperty("updated_by")
    val updatedBy: String? = null,
    @BsonProperty("updated_at")
    val updatedAt: Date? = null,
)


@Serializable
data class ProductCategoryDto(
    val id: String,
    val name: String,
    @SerialName("organisation_id")
    val organisationId: String,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("created_at")
    val createdAt: Long,
    @SerialName("updated_by")
    val updatedBy: String?,
    @SerialName("updated_at")
    val updatedAt: Long?,
)

fun ProductCategory.mapToDto() = ProductCategoryDto(
    id = this.id.toHexString(),
    name = this.name,
    organisationId = this.organisationId,
    createdBy = this.createdBy,
    createdAt = this.createdAt.time,
    updatedBy = this.updatedBy,
    updatedAt = this.updatedAt?.time,
)