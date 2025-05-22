package com.server.config

object EmailSenderConfig {
    val BASE_URL: String = System.getenv("EMAIL_SENDER_BASE_URL")
    val USERNAME: String = System.getenv("EMAIL_SENDER_USERNAME")
    val PASSWORD: String = System.getenv("EMAIL_SENDER_PASSWORD")
    val CONFIRM_EMAIL_TEMPLATE_ID: String = System.getenv("EMAIL_SENDER_CONFIRMATION_TEMPLATE_ID")
    val OTP_EMAIL_TEMPLATE_ID: String = System.getenv("EMAIL_SENDER_OTP_TEMPLATE_ID")
    val INVITE_TO_ORGANISATION_TEMPLATE_ID: String = System.getenv("EMAIL_SENDER_INVITE_TO_ORGANISATION_TEMPLATE_ID")
}