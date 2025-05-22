package com.server.features.email

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

class EmailTokenRepositoryImpl(
    private val db: MongoDatabase
) : EmailTokenRepository {

    private val tokensCollection = db.getCollection<EmailToken>("email_tokens")

    override suspend fun insert(token: EmailToken): Boolean {
        tokensCollection.createIndex(
            Indexes.ascending("expire_at"),
            IndexOptions().expireAfter(0, TimeUnit.DAYS)
        )
        return tokensCollection.insertOne(token).wasAcknowledged()
    }

    override suspend fun getByToken(token: String): EmailToken? {
        val filter = eq(EmailToken::token.name, token)
        return tokensCollection.find(filter).firstOrNull()
    }

    override suspend fun delete(token: String): Boolean {
        val filter = eq(EmailToken::token.name, token)
        return tokensCollection.deleteOne(filter).wasAcknowledged()
    }
}