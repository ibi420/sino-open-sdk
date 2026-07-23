package com.sino.sdk.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudPathHasherTest {

    @Test
    fun `hashPath should produce deterministic 64-char hex string`() {
        val salt = "SinoTestSalt1234567890".toByteArray()
        val path1 = "DCIM/Camera/vacation.jpg"
        val path2 = "DCIM/Camera/vacation.jpg"

        val hash1 = CloudPathHasher.hashPath(path1, salt)
        val hash2 = CloudPathHasher.hashPath(path2, salt)

        assertEquals(hash1, hash2)
        assertEquals(64, hash1.length) // HMAC-SHA256 hex string length
    }

    @Test
    fun `different paths should produce different hashes`() {
        val salt = "SinoTestSalt1234567890".toByteArray()
        val path1 = "DCIM/Camera/vacation.jpg"
        val path2 = "DCIM/Camera/taxes.pdf"

        val hash1 = CloudPathHasher.hashPath(path1, salt)
        val hash2 = CloudPathHasher.hashPath(path2, salt)

        assertNotEquals(hash1, hash2)
    }
}
