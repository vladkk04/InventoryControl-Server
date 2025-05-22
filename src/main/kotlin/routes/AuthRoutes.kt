package routes

import com.server.extractPrincipalUserId
import com.server.features.auth.requests.*
import com.server.features.auth.responses.AuthResponse
import com.server.features.auth.responses.AuthenticateResponse
import com.server.features.auth.responses.TokenResponse
import com.server.features.email.EmailConfirmationService
import com.server.features.email.EmailToken
import com.server.features.email.EmailTokenRepository
import com.server.features.emailSender.EmailSenderService
import com.server.features.organisation.OrganisationRole
import com.server.features.organisation.user.OrganisationUserRepository
import com.server.features.otp.EmailOtpRepository
import com.server.features.resetPassword.ResetPasswordToken
import com.server.features.resetPassword.ResetPasswordTokenRepository
import com.server.features.security.hashing.HashingService
import com.server.features.security.hashing.SaltedHash
import com.server.features.security.jwtToken.JwtTokenService
import com.server.features.user.User
import com.server.features.user.UserRepository
import com.server.handleQueryParameter
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.core.scope.get
import org.koin.ktor.plugin.scope
import java.util.*

fun Route.authRoutes(
    userRepository: UserRepository,
    hashingService: HashingService,
    resetPasswordTokenRepository: ResetPasswordTokenRepository,
    tokenService: JwtTokenService,
    emailSenderService: EmailSenderService,
    emailTokenRepository: EmailTokenRepository,
    emailOtpRepository: EmailOtpRepository
) {
    signIn(userRepository, hashingService, tokenService)
    signUp(userRepository, emailSenderService, emailTokenRepository, hashingService)
    forgotPassword(userRepository, emailSenderService)
    resetPassword(userRepository, resetPasswordTokenRepository, hashingService)
    validateOtpCode(emailOtpRepository)
    refreshToken()
    authenticate()
}

private fun Route.signIn(
    userRepository: UserRepository,
    hashingService: HashingService,
    tokenService: JwtTokenService
) {
    post("sign-in") {
        val request = call.receiveNullable<SignInRequest>()
            ?: return@post call.respond(HttpStatusCode.BadRequest)


        userRepository.getByEmail(request.email).let {
            val isValidPassword = hashingService.verifySaltedHash(
                value = request.password,
                saltHash = SaltedHash(
                    hash = it.password,
                    salt = it.salt
                )
            )
            if (!isValidPassword) {
                call.respond(HttpStatusCode.Conflict, "Incorrect password")
            }


            val accessToken = tokenService.createAccessToken(it.id.toString())
            val refreshToken = tokenService.createRefreshToken(it.id.toString())

            call.respond(
                HttpStatusCode.OK,
                AuthResponse(
                    accessToken = accessToken,
                    refreshToken = refreshToken
                )
            )
        }

        return@post call.respond(HttpStatusCode.BadRequest)
    }
}

private fun Route.signUp(
    userRepository: UserRepository,
    emailSenderService: EmailSenderService,
    emailTokenRepository: EmailTokenRepository,
    hashingService: HashingService
) {
    post("sign-up") {
        val request = call.receiveNullable<SignUpRequest>()
            ?: return@post call.respond(HttpStatusCode.BadRequest, "Bad Request")

        if (userRepository.isUserExists(request.email)) {
            return@post call.respond(HttpStatusCode.Conflict, "User already exist with this email")
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)

        val user = User(
            fullName = request.fullName,
            email = request.email,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )

        userRepository.insert(user, "User already exists")

        val createToken = UUID.randomUUID().toString()

        val emailTokenResult = emailTokenRepository.insert(
            EmailToken(
                email = user.email,
                token = createToken,
            )
        )

        if (!emailTokenResult) {
            call.respond(HttpStatusCode.Conflict, "Something went wrong")
            return@post
        }

        emailSenderService.sendConfirmationEmail(user.email, createToken)

        return@post call.respond(HttpStatusCode.Created, SignUpResponse(user.id.toHexString()))
    }

    get("sign-up-confirm") {

        val emailConfirmationService = call.scope.get<EmailConfirmationService>()

        val token = call.parameters["token"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Bad Request")

        if (emailConfirmationService.confirmEmail(token)) {
            call.respond(HttpStatusCode.Accepted, "Email confirmed successfully")
        } else {
            call.respond(HttpStatusCode.BadRequest, "Invalid, used, or expired token")
        }
    }
}

private fun Route.forgotPassword(
    userRepository: UserRepository,
    emailSenderService: EmailSenderService
) {
    post("forgot-password") {
        val request = call.receiveNullable<ForgotPasswordRequest>() ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            "Email is required"
        )

        userRepository.getByEmail(request.email).let {
            emailSenderService.sendForgotPasswordEmail(it.email)
            return@post call.respond(HttpStatusCode.OK)
        }
    }
}

private fun Route.resetPassword(
    userRepository: UserRepository,
    resetPasswordTokenRepository: ResetPasswordTokenRepository,
    hashingService: HashingService
) {
    post("reset-password") {
        val request = call.receiveNullable<ResetPasswordRequest>() ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            "Email is required and password is required"
        )

        if (!resetPasswordTokenRepository.isValid(request.token)) {
            return@post call.respond(
                HttpStatusCode.NotFound,
                "Token already expired or doesn't exist"
            )
        }

        val email = request.email
        val saltedHash = hashingService.generateSaltedHash(request.password)

        if (userRepository.updatePassword(email, saltedHash.hash, saltedHash.salt)) {
            resetPasswordTokenRepository.delete(request.token)
            return@post call.respond(HttpStatusCode.OK, "Password successfully updated")
        } else {
            call.respond(HttpStatusCode.BadRequest)

        }
    }
}

private fun Route.validateOtpCode(
    emailOtpRepository: EmailOtpRepository
) {
    get("validate-otp") {
        val email = call.handleQueryParameter("email") ?: return@get
        val otpCode = call.handleQueryParameter("otp") ?: return@get

        if (!emailOtpRepository.validateOneTimeOtp(email, otpCode)) {
            return@get call.respond(HttpStatusCode.Conflict, "Invalid OTP code")
        }

        val tokenService = call.scope.get<ResetPasswordTokenRepository>()

        val newToken = UUID.randomUUID().toString()

        if (tokenService.insert(ResetPasswordToken(newToken, email))) {
            call.respond(HttpStatusCode.OK, TokenResponse(newToken))
        } else {
            call.respond(HttpStatusCode.BadRequest, "Something went wrong")
        }
    }
}

private fun Route.refreshToken() {
    authenticate("jwt-refresh-auth") {
        post("refresh-token") {
            val tokenService = call.scope.get<JwtTokenService>()
            val principal = call.principal<JWTPrincipal>() ?: return@post call.respond(HttpStatusCode.Unauthorized)

            val userId = principal.payload.getClaim("userId").asString() ?: return@post call.respond(HttpStatusCode.BadRequest)

            val newAccessToken = tokenService.createAccessToken(userId)

            call.respond(HttpStatusCode.OK, TokenResponse(newAccessToken))
        }
    }
}

private fun Route.authenticate() {
    authenticate("jwt-auth") {
        get("authenticate") {
            val userId = call.extractPrincipalUserId() ?: return@get call.respond(HttpStatusCode.Unauthorized)
            call.respond(
                HttpStatusCode.OK,
                AuthenticateResponse(
                    userId = userId
                )
            )
        }
    }
}
