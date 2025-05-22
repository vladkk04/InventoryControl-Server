package com.server.features.emailSender

import com.server.features.otp.EmailOtpRepository
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class EmailSenderServiceImpl(
    private val emailOtpRepository: EmailOtpRepository,
    private val client: HttpClient
) : EmailSenderService {

    companion object {
        const val FROM = "Inventory Control <postmaster@inventory-control.click>"
        const val MAILGUN_BASE_URL = "https://api.eu.mailgun.net/v3/inventory-control.click/messages"
        const val MAILGUN_USERNAME = "api"
        const val MAILGUN_API_KEY = "0632f74b61dbad0fd6af1b6559e672cd-e71583bb-fd8adb17"
    }


    override suspend fun sendConfirmationEmail(email: String, token: String) {
        try {
            client.submitForm (System.getenv("MAILGUN_BASE_URL") ?: MAILGUN_BASE_URL) {
                basicAuth(System.getenv("MAILGUN_USERNAME") ?: MAILGUN_USERNAME, System.getenv("MAILGUN_API_KEY") ?: MAILGUN_API_KEY)
                parameter("from", FROM)
                parameter("to", email)
                parameter("subject", "Confirm your email")
                parameter("template", "confirm-email")
                parameter("t:variables", "{\"token\":\"$token\"}")
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    override suspend fun sendForgotPasswordEmail(email: String) {
        val otp = emailOtpRepository.generateOneTimeOtp(email)

        try {
            client.submitForm (System.getenv("MAILGUN_BASE_URL") ?: MAILGUN_BASE_URL) {
                basicAuth(System.getenv("MAILGUN_USERNAME") ?: MAILGUN_USERNAME, System.getenv("MAILGUN_API_KEY") ?: MAILGUN_API_KEY)
                parameter("from", FROM)
                parameter("to", email)
                parameter("subject", "Your otp code")
                parameter("template", "otp")
                parameter("t:variables", "{\"code\":\"$otp\"}")
            }
        } catch (e: Exception) {
            println(e)
        }
    }


}