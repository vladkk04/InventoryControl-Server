package com.server.features.file

import io.ktor.http.content.*

interface FileRepository {

    suspend fun uploadFile(multiPartData: MultiPartData, folderType: FolderType, fileName: String?): String?

    suspend fun getFileUrl(relativePath: String): String
}