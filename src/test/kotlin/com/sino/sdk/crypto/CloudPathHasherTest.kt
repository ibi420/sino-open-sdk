package com.sino.sdk.crypto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudPathHasherTest {

    @Test
    fun `computeCloudFolderHash should produce deterministic 16-char hex string`() {
        val salt = "SinoTestSalt1234567890".toByteArray()
        val path1 = "DCIM/Camera/vacation.jpg"
        val path2 = "DCIM/Camera/vacation.jpg"

        val hash1 = CloudPathHasher.computeCloudFolderHash(path1, salt)
        val hash2 = CloudPathHasher.computeCloudFolderHash(path2, salt)

        assertEquals(hash1, hash2)
        assertEquals(16, hash1.length)
    }

    @Test
    fun `computeCloudFileHash should produce deterministic 16-char hex string`() {
        val salt = "SinoTestSalt1234567890".toByteArray()
        val checksum = "sha256-hash-of-file-content"

        val hash = CloudPathHasher.computeCloudFileHash(checksum, salt)
        assertEquals(16, hash.length)
    }

    @Test
    fun `computeMetadataBatchHash should produce deterministic 12-char hex string with suffix`() {
        val salt = "SinoTestSalt1234567890".toByteArray()
        val folderId = 12345L

        val hash = CloudPathHasher.computeMetadataBatchHash(folderId, salt)
        
        assertTrue(hash.endsWith(".batch"))
        // 12 (hex) + 6 (.batch) = 18
        assertEquals(18, hash.length)
    }

    @Test
    fun `different inputs should produce different hashes`() {
        val salt = "SinoTestSalt1234567890".toByteArray()
        val path1 = "DCIM/Camera/vacation.jpg"
        val path2 = "DCIM/Camera/taxes.pdf"

        val hash1 = CloudPathHasher.computeCloudFolderHash(path1, salt)
        val hash2 = CloudPathHasher.computeCloudFolderHash(path2, salt)

        assertNotEquals(hash1, hash2)
    }
}
