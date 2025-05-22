package com.server.features.organisation.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderDiscount(
    val value: Double,
    val type: DiscountType
)
