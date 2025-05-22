package com.server.features.s3

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.*
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.sdk.kotlin.services.s3.presigners.presignPutObject
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.asByteStream
import io.ktor.client.request.*
import io.netty.util.internal.PlatformDependent.putObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.nio.file.Files
import kotlin.invoke
import kotlin.time.Duration.Companion.hours

class S3ServiceImpl(
    private val s3Client: S3Client
) : S3Service {

    companion object {
        private const val BUCKET_NAME = "inventory-app-test-bucket"
    }

    override suspend fun uploadFile(key: String, file: File): String {
        return withContext(Dispatchers.IO) {
            uploadFile(key, file.readBytes(), Files.probeContentType(file.toPath()))
        }
    }

    override suspend fun uploadFile(key: String, bytes: ByteArray, contentType: String): String {
        val request = PutObjectRequest {
            bucket = BUCKET_NAME
            this.key = key
            this.contentType = contentType
            this.body = ByteStream.fromBytes(bytes)
            this.acl = ObjectCannedAcl.PublicRead
        }

        s3Client.putObject(request)

        return "/$key"
    }

    override suspend fun getFileUrl(key: String): String {

       /* val request = GetObjectRequest{
            bucket = BUCKET_NAME
            this.key = key
        }

        val presignedRequest = s3Client.presignGetObject(request, 12.hours)


        return presignedRequest.url.toString()*/

        return ""
    }

    override suspend fun deleteFile(key: String): Boolean {

        val objectId = ObjectIdentifier {
            this.key = key.removePrefix("/")
        }

        val delOb = Delete {
            objects = listOf(objectId)
        }


        return try {
            val request = DeleteObjectsRequest {
                bucket = BUCKET_NAME
                delete = delOb
            }

            s3Client.deleteObjects(request)

            true
        } catch (e: Throwable ) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteFiles(keys: Set<String>): Boolean {

        val objectId = keys.map {
            ObjectIdentifier {
                this.key = it.removePrefix("/")
            }
        }

        val delOb = Delete {
            objects = objectId
        }

        val request = DeleteObjectsRequest {
            bucket = BUCKET_NAME
            delete = delOb
        }

        val response = s3Client.deleteObjects(request).deleted?.any { it.deleteMarker == true }

        return response == true
    }

}