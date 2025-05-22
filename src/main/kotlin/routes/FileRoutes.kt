package routes

import FileTooLargeException
import com.server.features.file.FileRepository
import com.server.features.file.FileResponse
import com.server.features.file.FolderType
import com.server.features.s3.S3Service
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import kotlinx.serialization.json.internal.decodeByReader
import org.koin.ktor.plugin.scope
import java.io.File
import java.util.UUID

fun Route.fileRoutes() {
    uploadFileRoute()
    getImage()
}

private fun Route.uploadFileRoute() {
    post("/upload") {
        try {
            val s3Service = call.scope.get<S3Service>()
            val multipart = call.receiveMultipart()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        val fileName = part.originalFileName.toString()
                        val bytes = part.provider().readRemaining().readByteArray()

                        val fileUrl = s3Service.uploadFile(
                            key = "uploads/$fileName",
                            bytes = bytes,
                            contentType = part.contentType.toString()
                        )

                        call.respond(HttpStatusCode.OK, FileResponse(fileUrl))
                    }
                    else -> {}
                }
                part.dispose()
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, e.message ?: "")
        }

    }
}

private fun Route.getImage() {
    get("/uploads/{fileName}") {
        val fileName = call.pathParameters["fileName"] ?: return@get call.respond(HttpStatusCode.BadRequest)
        val s3Service = call.scope.get<S3Service>()

        //s3Service.getFileUrl("uploads/${fileName}")

        return@get call.respond(HttpStatusCode.OK)
    }
}