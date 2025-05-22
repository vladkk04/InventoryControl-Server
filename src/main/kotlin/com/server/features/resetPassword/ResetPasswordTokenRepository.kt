package com.server.features.resetPassword

interface ResetPasswordTokenRepository {

    suspend fun getByToken(token: String): ResetPasswordToken?

    suspend fun isValid(token: String): Boolean

    suspend fun insert(token: ResetPasswordToken): Boolean

    suspend fun delete(token: String): Boolean
}