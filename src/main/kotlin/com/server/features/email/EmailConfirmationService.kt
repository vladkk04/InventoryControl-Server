package com.server.features.email

interface EmailConfirmationService {

    suspend fun confirmEmail(token: String): Boolean
}