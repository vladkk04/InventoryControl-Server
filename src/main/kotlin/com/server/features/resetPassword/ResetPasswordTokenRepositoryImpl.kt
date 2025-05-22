package com.server.features.resetPassword

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

class ResetPasswordTokenRepositoryImpl(
    private val db: MongoDatabase
) : ResetPasswordTokenRepository {

    private val tokensCollection = db.getCollection<ResetPasswordToken>("reset_password_tokens")

    override suspend fun insert(token: ResetPasswordToken): Boolean {
        tokensCollection.createIndex(
            Indexes.ascending("expire_at"),
            IndexOptions().expireAfter(0, TimeUnit.DAYS)
        )
        return tokensCollection.insertOne(token).wasAcknowledged()
    }

    override suspend fun getByToken(token: String): ResetPasswordToken? {
        val filter = eq(ResetPasswordToken::token.name, token)
        return tokensCollection.find(filter).firstOrNull()
    }

    override suspend fun isValid(token: String): Boolean {
        return getByToken(token) != null
    }

    override suspend fun delete(token: String): Boolean {
        val filter = eq(ResetPasswordToken::token.name, token)
        return tokensCollection.deleteOne(filter).wasAcknowledged()
    }
}