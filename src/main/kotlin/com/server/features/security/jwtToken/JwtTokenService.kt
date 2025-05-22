package com.server.features.security.jwtToken

import com.auth0.jwt.JWTVerifier
import com.server.features.organisation.OrganisationRole
import io.ktor.server.auth.jwt.*

interface JwtTokenService {

    val verifier: JWTVerifier

    fun validatorAccess(credential: JWTCredential): JWTPrincipal?

    fun validatorRefresh(credential: JWTCredential): JWTPrincipal?

    fun createAccessToken(userId: String): String

    fun createRefreshToken(userId: String): String
}
