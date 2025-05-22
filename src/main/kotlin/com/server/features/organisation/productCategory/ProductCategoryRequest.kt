package com.server.features.organisation.productCategory;

import kotlinx.serialization.Serializable

@Serializable
data class ProductCategoryRequest(
    val name: String,
)