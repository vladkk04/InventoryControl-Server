package com.server.exceptions

import com.mongodb.MongoException
import com.mongodb.WriteError
import org.bson.BsonDocument
import org.bson.conversions.Bson

sealed class DatabaseException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable?) : super(message, cause)

    data class DuplicateKeyException(
        val writeError: WriteError,
        override val message: String = "'${extractDuplicateKeyDetails(writeError).second}' already exist",
        override val cause: Throwable? = null
    ) : DatabaseException(message, cause)

    class NotFoundException(
        val entityName: String,
        val id: Any? = null,
        override val message: String = "$entityName not found" + (id?.let { " with id $it" })
    ): DatabaseException(message)

    data class OperationException(
        val operation: OperationDatabase,
        override val message: String = "${OperationDatabase::name} operation failed",
        override val cause: Throwable? = null
    ) : DatabaseException(message, cause)

    data class ValidationException(
        val field: String,
        val collection: Any,
        override val message: String = "Invalid format",
        override val cause: IllegalArgumentException? = null
    ): DatabaseException(message, cause)

    data class GenerationException(
        override val message: String? = null,
        override val cause: Throwable? = null
    ): DatabaseException(message ?: "\"Something went wrong\"", cause)

}


private fun extractDuplicateKeyDetails(error: WriteError): Pair<String?, String?> {
    return try {
        val message = error.message ?: return null to null
        val regex = Regex("dup key: \\{ ([\\w.]+): (.+) }")
        val match = regex.find(message)

        if (match != null) {
            match.groupValues[1] to match.groupValues[2].removeSurrounding("\"")
        } else {
            val keyValue = (error.details["keyValue"] as? BsonDocument)
            keyValue?.let { doc ->
                doc.firstKey to doc[doc.firstKey]?.toString()
            } ?: (null to null)
        }
    } catch (e: Exception) {
        null to null
    }
}