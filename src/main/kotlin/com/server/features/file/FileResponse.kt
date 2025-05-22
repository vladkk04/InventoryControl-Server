package com.server.features.file

import kotlinx.serialization.Serializable

@Serializable
data class FileResponse(
    val url : String,
)
