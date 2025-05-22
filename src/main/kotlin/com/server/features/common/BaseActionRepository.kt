package com.server.features.common

import com.mongodb.client.model.DeleteOptions
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.kotlin.client.coroutine.AggregateFlow
import org.bson.conversions.Bson

abstract class BaseActionRepository<T : Any> {

    abstract suspend fun insert(value: T, errorMessage: String?): Boolean

    abstract suspend fun getById(id: String, errorMessage: String): T?

    abstract suspend fun deleteById(id: String): Boolean

    protected abstract suspend fun deleteOne(filter: Bson, options: DeleteOptions): Boolean

    protected abstract suspend fun getAll(filter: Bson): List<T>

    protected abstract suspend fun getBy(filter: Bson, errorMessage: String): T?

    protected abstract suspend fun replaceOne(filter: Bson, replacement: T, replaceOptions: ReplaceOptions): Boolean

    protected abstract suspend fun updateById(id: String, update: Bson, updateOptions: UpdateOptions): Boolean

    protected abstract suspend fun updateOne(filter: Bson, update: Bson, updateOptions: UpdateOptions): Boolean

    protected abstract suspend fun deleteMany(filter: Bson, options: DeleteOptions): Boolean
}


