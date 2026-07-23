package com.sino.sdk.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DuressKeyDerivationTest {

    private val duressDerivation = DuressKeyDerivation()

    @Test
    fun `primary key and decoy key derived from same password MUST be cryptographically distinct`() {
        val password = "UserSecretPassphrase123!"
        val userSalt = "UserSpecificSaltValue99".toByteArray()

        val primaryKey = duressDerivation.deriveVaultKey(password, userSalt, isDuress = false)
        val decoyKey = duressDerivation.deriveVaultKey(password, userSalt, isDuress = true)

        assertEquals(32, primaryKey.size)
        assertEquals(32, decoyKey.size)
        assertFalse("Primary key and Decoy key MUST NOT match", primaryKey.contentEquals(decoyKey))
    }
}
