package com.server.features.organisation.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderProduct(
    @SerialName("product_id")
    val productId: String,
    val quantity: Double,
    val price: Double,
)
