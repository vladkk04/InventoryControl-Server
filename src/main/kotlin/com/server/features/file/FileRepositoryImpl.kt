import com.server.features.file.FileRepository
import com.server.features.file.FolderType
import com.server.save
import io.ktor.http.content.*
import io.ktor.utils.io.*
import io.ktor.utils.io.streams.*
import java.io.File
import java.util.*

class FileRepositoryImpl : FileRepository {
    private val baseUploadDir = "uploads"
    private val maxFileSize = 10 * 1024 * 1024 // 10MB limit

    init {
        File(baseUploadDir).mkdirs()
    }

    override suspend fun uploadFile(
        multiPartData: MultiPartData,
        folderType: FolderType,
        fileName: String?
    ): String? {
        var savedFilePath: String? = null

        try {
            multiPartData.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val tempFile = File.createTempFile("upload", ".tmp")
                        try {
                            part.save(tempFile)
                            val fileSize = tempFile.length()

                            if (fileSize > maxFileSize) {
                                throw FileTooLargeException("File exceeds maximum size of ${maxFileSize / (1024 * 1024)}MB")
                            }

                            val originalName = part.originalFileName ?: fileName ?: "file"
                            val fileExtension = originalName.substringAfterLast('.', "").takeIf { it.isNotEmpty() }
                                ?: getExtensionFromContentType(part.contentType?.toString())
                                ?: "bin"

                            val safeFileName = generateSafeFileName(originalName, fileExtension)

                            val targetFolder = getFolderForType(folderType)
                            val targetFile = File(targetFolder, safeFileName)

                            tempFile.copyTo(targetFile, overwrite = true)

                            savedFilePath = "${folderType.name.lowercase()}/$safeFileName"
                        } finally {
                            tempFile.delete()
                        }
                    }
                    else -> part.dispose()
                }
            }
            return savedFilePath
        } catch (e: Exception) {
            savedFilePath?.let { path ->
                File("$baseUploadDir/$path").delete()
            }
            throw e
        }
    }

    private fun getExtensionFromContentType(contentType: String?): String? {
        return when (contentType?.substringBefore(';')?.trim()?.lowercase()) {
            "application/pdf" -> "pdf"
            "application/msword" -> "doc"
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> "docx"
            "text/plain" -> "txt"
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "application/zip" -> "zip"
            else -> null
        }
    }

    private fun getFolderForType(folderType: FolderType): File {
        return File(baseUploadDir, folderType.name.lowercase()).apply {
            mkdirs()
        }
    }

    private fun generateSafeFileName(originalName: String, extension: String): String {
        val baseName = originalName.substringBeforeLast('.')
            .replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            .take(100)

        return "${baseName}_${UUID.randomUUID().toString().take(8)}.$extension".lowercase()
    }

    override suspend fun getFileUrl(relativePath: String): String {
        return "/files/$relativePath"
    }
}


class FileTooLargeException(message: String) : Exception(message)