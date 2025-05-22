package com.server.features.organisation.order

import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    val url: String,
    val size: String
)
