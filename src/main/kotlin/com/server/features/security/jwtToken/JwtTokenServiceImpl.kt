package com.server.features.security.jwtToken

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.server.features.organisation.OrganisationRole
import io.ktor.server.auth.jwt.*
import java.util.*

class JwtTokenServiceImpl(
    private val jwtConfig: JwtTokenConfig
) : JwtTokenService {

    override val verifier: JWTVerifier
        get() = JWT
            .require(Algorithm.HMAC256(jwtConfig.secretKey))
            .withIssuer(jwtConfig.issuer)
            .withAudience(jwtConfig.audience)
            .build()

    override fun validatorAccess(credential: JWTCredential): JWTPrincipal? {
        val userId = extractUserId(credential)
        val tokenType = extractTokenType(credential)

        return if (audienceMatches(credential) && userId != null && tokenType != null && tokenType == JwtTokenType.ACCESS) {
            JWTPrincipal(credential.payload)
        } else {
            null
        }
    }

    override fun validatorRefresh(credential: JWTCredential): JWTPrincipal? {
        val userId = extractUserId(credential)
        val tokenType = extractTokenType(credential)

        return if (audienceMatches(credential) && userId != null && tokenType != null && tokenType == JwtTokenType.REFRESH) {
            JWTPrincipal(credential.payload)
        } else {
            null
        }
    }

    override fun createAccessToken(userId: String): String {
        return createJwtToken(
            config = jwtConfig.copy(expiresIn = (30 * 24 * 60 * 60 * 1000L)),
            claims = arrayOf(
                JwtTokenClaim(
                    name = "userId",
                    userId
                ),
                JwtTokenClaim(
                    name = "tokenType",
                    JwtTokenType.ACCESS.name
                )
            )
        )
    }


    override fun createRefreshToken(userId: String): String {
        return createJwtToken(
            config = jwtConfig.copy(expiresIn = (12 * 60 * 60 * 1000L)),
            claims = arrayOf(
                JwtTokenClaim(
                    "userId",
                    userId
                ),
                JwtTokenClaim(
                    name = "tokenType",
                    JwtTokenType.REFRESH.name
                )
            )
        )
    }


    private fun createJwtToken(config: JwtTokenConfig, vararg claims: JwtTokenClaim): String {
        val token = JWT
            .create()
            .withAudience(config.audience)
            .withIssuer(config.issuer)
            .withExpiresAt(Date(System.currentTimeMillis() + config.expiresIn))

        claims.forEach { claim -> token.withClaim(claim.name, claim.value) }

        return token.sign(Algorithm.HMAC256(config.secretKey))
    }

    private fun audienceMatches(credential: JWTCredential): Boolean =
        credential.payload.audience.contains(jwtConfig.audience)

    private fun extractTokenType(credential: JWTCredential): JwtTokenType? {
        return when (credential.payload.getClaim("tokenType").asString()) {
            JwtTokenType.ACCESS.name -> JwtTokenType.ACCESS
            JwtTokenType.REFRESH.name -> JwtTokenType.REFRESH
            else -> { null }
        }
    }

    private fun extractUserId(credential: JWTCredential): String? = credential.payload.getClaim("userId").asString()

}