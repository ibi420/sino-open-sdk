package com.sino.sdk.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode

/**
 * Verifiable implementation of Sino's password-based key derivation.
 * Uses Argon2id for high-security resistance against GPU-based cracking.
 */
class SinoKeyDerivation {

    private val argon2Kt = Argon2Kt()

    companion object {
        // Argon2id parameters (OWASP recommended minimums)
        private const val ITERATIONS = 3
        private const val MEMORY_KIB = 65536 // 64 MB
        private const val PARALLELISM = 4
        private const val HASH_LENGTH = 32 // 256 bits for AES-256
    }

    fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        try {
            val result = argon2Kt.hash(
                mode = Argon2Mode.ARGON2_ID,
                password = passwordBytes,
                salt = salt,
                tCostInIterations = ITERATIONS,
                mCostInKibibyte = MEMORY_KIB,
                parallelism = PARALLELISM,
                hashLengthInBytes = HASH_LENGTH
            )
            val hash = ByteArray(HASH_LENGTH)
            result.rawHash.rewind()
            result.rawHash.get(hash)
            return hash
        } finally {
            SecurityUtils.fillZero(passwordBytes)
        }
    }
}
