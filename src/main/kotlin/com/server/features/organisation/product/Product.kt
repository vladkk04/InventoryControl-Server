package com.server.features.organisation.product;

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.Instant
import java.util.Date

data class Product(
    @BsonId
    val id: ObjectId = ObjectId(),
    @BsonProperty("image_url")
    val imageUrl: String? = null,
    val name: String,
    val barcode: String,
    val quantity: Double,
    val unit: ProductUnit,
    @BsonProperty("min_stock_level")
    val minStockLevel: Double,
    val description: String? = null,
    val tags: List<ProductTag> = emptyList(),
    val updates: List<ProductUpdateHistory> = emptyList(),
    @BsonProperty("category_id")
    val categoryId: String? = null,
    @BsonProperty("organisation_id")
    val organisationId: String,
    @BsonProperty("created_by")
    val createdBy: String,
)

@Serializable
data class ProductDto(
    val id: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val name: String,
    val barcode: String,
    val quantity: Double,
    val unit: ProductUnit,
    @SerialName("category_id")
    val categoryId: String? = null,
    @SerialName("min_stock_level")
    val minStockLevel: Double,
    val description: String? = null,
    @SerialName("organisation_id")
    val organisationId: String,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("created_at")
    val createdAt: Long,
    val tags: List<ProductTag> = emptyList(),
    val updates: List<ProductUpdateHistory> = emptyList()
)

fun Product.mapToDto() =
    ProductDto(
        id = this.id.toHexString(),
        imageUrl = this.imageUrl,
        name = this.name,
        barcode = this.barcode,
        quantity = this.quantity,
        unit = this.unit,
        categoryId = this.categoryId,
        minStockLevel = this.minStockLevel,
        description = this.description,
        createdAt = this.id.date.time,
        organisationId = this.organisationId,
        createdBy = this.createdBy,
        tags = this.tags,
        updates = this.updates
    )