package com.sino.sdk.crypto

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

/**
 * Verifiable implementation of Sino's password-based key derivation.
 * Uses Argon2id for high-security resistance against GPU-based cracking.
 * Implemented using pure BouncyCastle JVM for 100% cross-platform auditability.
 */
class SinoKeyDerivation {

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
            val builder = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(ITERATIONS)
                .withMemoryAsKB(MEMORY_KIB)
                .withParallelism(PARALLELISM)
                .withSalt(salt)

            val generator = Argon2BytesGenerator()
            generator.init(builder.build())

            val result = ByteArray(HASH_LENGTH)
            generator.generateBytes(passwordBytes, result, 0, result.size)
            return result
        } finally {
            SecurityUtils.fillZero(passwordBytes)
        }
    }
}
