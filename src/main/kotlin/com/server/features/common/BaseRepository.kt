package com.server.features.common

import com.mongodb.MongoException
import com.mongodb.MongoWriteException
import com.mongodb.client.model.*
import com.mongodb.client.model.Filters.eq
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import com.server.exceptions.DatabaseException
import com.server.exceptions.OperationDatabase
import io.ktor.util.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import org.bson.types.ObjectId

abstract class BaseRepository<T : Any> : BaseActionRepository<T>() {

    protected abstract val db: MongoDatabase
    protected abstract val collection: MongoCollection<T>

    private val collectionName: String
        get() = collection.namespace.collectionName

    private val entityName: String
        get() = this::class.simpleName!!

    override suspend fun insert(value: T, errorMessage: String?) = try {
        collection.insertOne(value).wasAcknowledged()
    } catch (e: MongoWriteException) {
        when (e.code) {
            11000 -> throw DatabaseException.DuplicateKeyException(
                writeError = e.error,
                message = errorMessage ?: e.localizedMessage,
            )

            else -> throw DatabaseException.OperationException(
                operation = OperationDatabase.INSERT,
                cause = e,
            )
        }
    }

    override suspend fun getBy(filter: Bson, errorMessage: String): T = try {
        collection.find(filter).firstOrNull() ?: throw DatabaseException.NotFoundException(
            entityName = entityName,
            message = errorMessage
        )
    } catch (e: MongoException) {
        throw DatabaseException.OperationException(
            operation = OperationDatabase.FIND,
            message = errorMessage,
            cause = e
        )
    }

    override suspend fun getById(id: String, errorMessage: String): T = try {
        require(ObjectId.isValid(id)) { "Invalid ObjectId format" }
        getBy(eq("_id", ObjectId(id)), errorMessage)
    } catch (e: IllegalArgumentException) {
        throw DatabaseException.ValidationException(
            field = "id",
            cause = e,
            collection = collectionName
        )
    }

    override suspend fun getAll(filter: Bson): List<T> = try {
        collection.find(filter).toList()
    } catch (e: MongoException) {
        throw DatabaseException.OperationException(
            operation = OperationDatabase.GET_ALL,
            cause = e,
        )
    }

    override suspend fun updateById(
        id: String,
        update: Bson,
        updateOptions: UpdateOptions
    ) = try {
        require(ObjectId.isValid(id)) { "Invalid ObjectId format" }
        updateOne(eq("_id", ObjectId(id)), update, updateOptions)
    } catch (e: IllegalArgumentException) {
        throw DatabaseException.ValidationException(
            field = "id",
            cause = e,
            collection = collectionName
        )
    }

    override suspend fun updateOne(
        filter: Bson,
        update: Bson,
        updateOptions: UpdateOptions
    ) = try {
        collection.updateOne(filter, update, updateOptions).wasAcknowledged()
    } catch (e: MongoException) {
        false &&
        throw DatabaseException.OperationException(
            operation = OperationDatabase.UPDATE,
            cause = e
        )
    }

    override suspend fun replaceOne(
        filter: Bson,
        replacement: T,
        replaceOptions: ReplaceOptions
    ) = try {
        collection.replaceOne(filter, replacement, replaceOptions).wasAcknowledged()
    } catch (e: MongoException) {
        throw DatabaseException.OperationException(
            operation = OperationDatabase.REPLACE,
            cause = e
        )
    }

    override suspend fun deleteById(id: String): Boolean = try {
        require(ObjectId.isValid(id)) { "Invalid ObjectId format" }
        deleteOne(eq("_id", ObjectId(id)), DeleteOptions())
    } catch (e: IllegalArgumentException) {
        throw DatabaseException.ValidationException(
            field = "id",
            cause = e,
            collection = collectionName
        )
    }

    override suspend fun deleteOne(
        filter: Bson,
        options: DeleteOptions
    ) = try {
        collection.deleteOne(filter, options).wasAcknowledged()
    } catch (e: MongoException) {
        throw DatabaseException.OperationException(
            operation = OperationDatabase.DELETE,
            cause = e,
        )
    }

    override suspend fun deleteMany(
        filter: Bson,
        options: DeleteOptions
    ) = try {
        collection.deleteMany(filter, options).wasAcknowledged()
    } catch (e: MongoException) {
        throw DatabaseException.OperationException(
            OperationDatabase.DELETE_MANY,
            cause = e,
        )
    }

}

suspend inline fun <reified T : Any> MongoCollection<*>.safeAggregate(
    pipeline: List<Bson>
): List<T> = try {
    this.withDocumentClass(T::class.java)
        .aggregate(pipeline)
        .toList()
} catch (e: Exception) {
    throw DatabaseException.GenerationException(cause = e)
}
