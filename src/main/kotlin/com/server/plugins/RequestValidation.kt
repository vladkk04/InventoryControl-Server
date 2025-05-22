package com.server.plugins

import com.server.features.auth.requests.*
import com.server.features.organisation.OrganisationRequest
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*
import java.util.regex.Pattern

private val VALID_EMAIL_ADDRESS_REGEX: Pattern = Pattern.compile(
    "[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?",
    Pattern.CASE_INSENSITIVE
)


fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate<SignUpRequest> { request ->
            validateEmailAndPassword(request.email, request.password)
        }

        validate<SignInRequest> { request ->
            validateEmailAndPassword(request.email, request.password)
        }

        validate<ResetPasswordRequest> { request ->
            validateEmailAndPassword(request.email, request.password)
        }

        validate<ForgotPasswordRequest> { request ->
            validateEmail(request.email)
        }

        validate<OtpRequest> { request ->
            validateEmail(request.email) +
                    validateOtp(request.otpCode)
        }

        validate<OrganisationRequest> { request ->
            validateField(request.name, "Name") +
                    validateField(request.currency, "Currency")
        }

    }
}

private fun validateField(value: String, fieldName: String): ValidationResult {
    return when {
        value.isBlank() -> ValidationResult.Invalid("$fieldName cannot be empty")
        else -> ValidationResult.Valid
    }
}

private fun <T> validateList(value: List<T>, fieldName: String): ValidationResult {
    return when {
        value.isEmpty() -> ValidationResult.Invalid("$fieldName list cannot be empty")
        else -> ValidationResult.Valid
    }
}

private fun validateEmail(email: String): ValidationResult {
    return when {
        email.isBlank() -> ValidationResult.Invalid("Email cannot be empty")

        else -> if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).matches()) {
            ValidationResult.Invalid("Invalid email format")
        } else ValidationResult.Valid
    }
}


private fun validatePassword(password: String): ValidationResult {
    return when {
        password.isBlank() -> ValidationResult.Invalid("Password cannot be empty")
        else -> ValidationResult.Valid
    }
}

private fun validateOtp(otp: String): ValidationResult {
    return when {
        otp.isBlank() -> ValidationResult.Invalid("Otp cannot be empty")
        otp.length < 6 -> ValidationResult.Invalid("Otp must be 6 numbers")
        else -> ValidationResult.Valid
    }
}

private fun validateEmailAndPassword(email: String, password: String): ValidationResult {
    return validateEmail(email) + validatePassword(password)
}

private operator fun ValidationResult.plus(other: ValidationResult): ValidationResult {
    return when {
        this is ValidationResult.Invalid -> this
        other is ValidationResult.Invalid -> other
        else -> ValidationResult.Valid
    }
}