package com.server.features.security.hashing

data class SaltedHash(
    val hash: String,
    val salt: String
)
