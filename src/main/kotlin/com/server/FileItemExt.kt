package com.server

import io.ktor.http.content.*
import io.ktor.utils.io.*
import java.io.File


suspend fun PartData.FileItem.save(file: File): String {
    val fileBytes = provider().toByteArray()
    file.writeBytes(fileBytes)
    return file.path
}