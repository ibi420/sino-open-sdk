package com.sino.sdk.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class SinoKeyDerivationTest {

    private val derivation = SinoKeyDerivation()

    @Test
    fun `deriveKey should produce 32-byte key deterministically`() {
        val password = "MasterPassword2026"
        val salt = "FixedSaltValueForTesting".toByteArray()

        val key1 = derivation.deriveKey(password, salt)
        val key2 = derivation.deriveKey(password, salt)

        assertEquals(32, key1.size)
        assertArrayEquals(key1, key2)
    }
}
