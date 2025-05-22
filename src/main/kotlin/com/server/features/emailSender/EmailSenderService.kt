package com.server.features.emailSender

interface EmailSenderService {

    suspend fun sendConfirmationEmail(email: String, token: String)

    suspend fun sendForgotPasswordEmail(email: String)

}