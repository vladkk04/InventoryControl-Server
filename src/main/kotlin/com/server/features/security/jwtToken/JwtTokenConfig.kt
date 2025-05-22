package com.server.features.security.jwtToken

data class JwtTokenConfig(
    val issuer: String,
    val audience: String,
    val expiresIn: Long,
    val secretKey: String
)
