package com.server.features.email

import com.server.features.user.UserRepository

class EmailConfirmationServiceImpl(
    private val userRepository: UserRepository,
    private val emailTokenRepository: EmailTokenRepository
) : EmailConfirmationService {

    override suspend fun confirmEmail(token: String): Boolean {
        val storedToken = emailTokenRepository.getByToken(token) ?: return false

        return if (emailTokenRepository.delete(token)) {
            userRepository.confirmEmail(storedToken.email)
            true
        } else {
            false
        }
    }
}