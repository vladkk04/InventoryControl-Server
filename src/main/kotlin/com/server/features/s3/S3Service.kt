package com.server.features.s3

import java.io.File

interface S3Service {
    suspend fun uploadFile(key: String, file: File): String

    suspend fun uploadFile(key: String, bytes: ByteArray, contentType: String): String

    suspend fun getFileUrl(key: String): String

    suspend fun deleteFile(key: String): Boolean?

    suspend fun deleteFiles(keys: Set<String>): Boolean?
}