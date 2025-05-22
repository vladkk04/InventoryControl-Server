package com.server.features.updateStockProduct

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

@Serializable
data class UpdateStockProductRequest(
    @SerialName("products")
    val productsUpdates: List<ProductStockUpdate>,
    @SerialName("order_id")
    val orderId: String? = null,
)
