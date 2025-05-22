package com.server.features.updateStockProduct;

import com.server.features.organisation.product.Product
import com.server.features.organisation.product.ProductUnit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class ProductUpdateStock(
    @BsonId
    val id: ObjectId = ObjectId(),
    @BsonProperty("organisation_id")
    val organisationId: String,
    @BsonProperty("products")
    val productsUpdates: List<ProductStockUpdate>,
    @BsonProperty("updated_by")
    val updatedBy: String,
    @BsonProperty("order_id")
    val orderId: String? = null,
)

@Serializable
data class ProductUpdateStockDto(
    val id: String,
    @SerialName("organisation_id")
    val organisationId: String,
    @SerialName("products")
    val productsUpdates: List<ProductStockUpdate>,
    @SerialName("updated_by")
    val updatedBy: String,
    @SerialName("updated_at")
    val updateAt: Long,
)

@Serializable
data class ProductUpdateStockViewDto(
    val id: String,
    val products: List<ProductWithDetails>,
    @SerialName("updated_by")
    val updatedBy: String,
    @SerialName("updated_at")
    val updatedAt: Long
)


@Serializable
data class ProductWithDetails(
    @SerialName("product_id")
    val productId: String,
    val name: String,
    val unit: ProductUnit,
    @SerialName("previous_stock")
    val previousStock: Double,
    @SerialName("adjustment")
    val adjustmentValue: Double
)


@Serializable
data class ChangeStockProduct(
    val id: String,
    @SerialName("previous_stock")
    val previousStock: Double,
    @SerialName("adjustment")
    val adjustmentValue: Double,
    @SerialName("updated_by")
    val updatedBy: String,
    @SerialName("updated_at")
    val updateAt: Long,
)



fun ProductUpdateStock.mapToDto() = ProductUpdateStockDto(
    id = this.id.toHexString(),
    organisationId = this.organisationId,
    productsUpdates = this.productsUpdates,
    updatedBy = this.updatedBy,
    updateAt = this.id.date.time
)