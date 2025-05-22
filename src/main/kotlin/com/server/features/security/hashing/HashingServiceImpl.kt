package com.server.features.security.hashing

import com.mongodb.internal.HexUtils
import java.security.MessageDigest
import java.security.SecureRandom

class HashingServiceImpl: HashingService {

    override fun generateSaltedHash(value: String, saltLength: Int): SaltedHash {
        val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
        val saltAsHex = HexUtils.toHex(salt)
        val hash = hashString(value + saltAsHex)
        return SaltedHash(
            hash = hash,
            salt = saltAsHex
        )
    }

    override fun verifySaltedHash(value: String, saltHash : SaltedHash): Boolean {
        val combinedInput = value + saltHash.salt
        val hashedInput = hashString(combinedInput)

        return hashedInput == saltHash.hash
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun hashString(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .toHexString()
    }
}