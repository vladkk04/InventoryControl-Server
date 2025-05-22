package com.server.features.email

interface EmailTokenRepository {



    suspend fun getByToken(token: String): EmailToken?

    suspend fun insert(token: EmailToken): Boolean

    suspend fun delete(token: String): Boolean
}