package com.sino.sdk.crypto

import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AESEncryptionEngineTest {

    private val engine = AESEncryptionEngine()

    @Test
    fun `test basic encryption and decryption`() {
        val originalText = "Sino Zero-Trust Security Verification"
        val password = "StrongMasterPassword123"
        val salt = engine.generateIV() // Using IV generator for random salt
        
        // Use KEK derivation
        val derivation = SinoKeyDerivation()
        val key = derivation.deriveKey(password, salt)
        val iv = engine.generateIV()

        // 1. Encrypt
        val inputStream = ByteArrayInputStream(originalText.toByteArray())
        val encryptedOutputStream = ByteArrayOutputStream()
        engine.encrypt(inputStream, encryptedOutputStream, key, iv)
        
        val encryptedData = encryptedOutputStream.toByteArray()
        assertTrue(encryptedData.isNotEmpty())
        assertTrue(encryptedData.toString(Charsets.UTF_8) != originalText)

        // 2. Decrypt
        val encryptedInputStream = ByteArrayInputStream(encryptedData)
        val decryptedOutputStream = ByteArrayOutputStream()
        engine.decrypt(encryptedInputStream, decryptedOutputStream, key, iv)
        
        val decryptedText = decryptedOutputStream.toString(Charsets.UTF_8)
        assertEquals(originalText, decryptedText)
    }

    @Test
    fun `test chunked encryption for streaming`() {
        // Create 2.5MB of data to test multiple 1MB chunks
        val size = (2.5 * 1024 * 1024).toInt()
        val originalData = ByteArray(size) { it.toByte() }
        val key = engine.generateDEK()
        val iv = engine.generateIV()

        // 1. Encrypt Chunked
        val inputStream = ByteArrayInputStream(originalData)
        val encryptedOutputStream = ByteArrayOutputStream()
        engine.encryptChunked(inputStream, encryptedOutputStream, key, iv, chunkSize = 1024 * 1024)
        
        val encryptedData = encryptedOutputStream.toByteArray()
        
        // 2. Decrypt Chunked
        val encryptedInputStream = ByteArrayInputStream(encryptedData)
        val decryptedOutputStream = ByteArrayOutputStream()
        engine.decryptChunked(encryptedInputStream, decryptedOutputStream, key, iv, chunkSize = 1024 * 1024)
        
        val decryptedData = decryptedOutputStream.toByteArray()
        assertTrue(originalData.contentEquals(decryptedData))
    }
}
