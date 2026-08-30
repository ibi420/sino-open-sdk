package com.sino.sdk.crypto

import com.sino.sdk.crypto.SecurityUtils
import com.sino.sdk.crypto.SinoKeyDerivation
import java.security.MessageDigest

/**
 * Open Specification for Sino's Dual-Vault Duress Key Derivation.
 * Demonstrates how Decoy Vault Keys are derived independently from Primary Vault Keys
 * using distinct domain-separated Argon2 salts, ensuring zero mathematical linkage.
 */
class DuressKeyDerivation {

    private val keyDerivation = SinoKeyDerivation()

    companion object {
        private val PRIMARY_SALT_PREFIX = "SinoPrimaryMasterKeySaltV1".toByteArray(Charsets.UTF_8)
        private val DURESS_SALT_PREFIX = "SinoDuressDecoyKeySaltV1".toByteArray(Charsets.UTF_8)
    }

    /**
     * Derives a vault key using domain separation.
     *
     * @param password User input PIN or password (as CharArray for memory hygiene)
     * @param userSpecificSalt Per-user salt
     * @param isDuress True for Decoy Vault, false for Primary Vault
     * @return 256-bit derived key
     */
    fun deriveVaultKey(password: CharArray, userSpecificSalt: ByteArray, isDuress: Boolean): ByteArray {
        val prefix = if (isDuress) DURESS_SALT_PREFIX else PRIMARY_SALT_PREFIX
        val combinedSalt = computeCombinedSalt(prefix, userSpecificSalt)

        try {
            return keyDerivation.deriveKey(password, combinedSalt)
        } finally {
            SecurityUtils.fillZero(combinedSalt)
        }
    }

    private fun computeCombinedSalt(prefix: ByteArray, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(prefix)
        digest.update(salt)
        return digest.digest()
    }
}
