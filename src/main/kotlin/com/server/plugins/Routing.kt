package com.server.plugins

import com.server.features.email.EmailTokenRepository
import com.server.features.emailSender.EmailSenderService
import com.server.features.otp.EmailOtpRepository
import com.server.features.resetPassword.ResetPasswordTokenRepository
import com.server.features.security.hashing.HashingService
import com.server.features.security.jwtToken.JwtTokenService
import com.server.features.user.UserRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import routes.*
import routes.organisation.organisationRoutes

fun Application.configureRouting() {

    val hashingService by inject<HashingService>()
    val userRepository by inject<UserRepository>()
    val resetPasswordTokenRepository by inject<ResetPasswordTokenRepository>()
    val tokenService by inject<JwtTokenService>()
    val emailSenderService by inject<EmailSenderService>()
    val emailTokenRepository by inject<EmailTokenRepository>()
    val emailOtpRepository by inject<EmailOtpRepository>()

    routing {
        authRoutes(
            userRepository,
            hashingService,
            resetPasswordTokenRepository,
            tokenService,
            emailSenderService,
            emailTokenRepository,
            emailOtpRepository
        )

        fileRoutes()

        authenticate("jwt-auth") {
            organisationRoutes()
            userRoutes()
            profileRoutes()
        }
    }
}
