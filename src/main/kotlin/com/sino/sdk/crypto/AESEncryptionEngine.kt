package com.sino.sdk.crypto

import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Core engine for AES-256-GCM encryption and decryption.
 * Verifiable implementation of Sino's Zero-Knowledge encryption.
 */
class AESEncryptionEngine : EncryptionEngine {

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val TAG_LENGTH_BITS = 128
        private const val IV_LENGTH_BYTES = 12
        private const val KEY_SIZE_BYTES = 32 // 256 bits
    }

    override fun encrypt(
        inputStream: InputStream,
        outputStream: OutputStream,
        key: ByteArray,
        iv: ByteArray
    ) {
        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        val secretKey = SecretKeySpec(key, "AES")
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val buffer = ByteArray(8192)
        try {
            var bytesRead = inputStream.read(buffer)
            while (bytesRead != -1) {
                val encrypted = cipher.update(buffer, 0, bytesRead)
                if (encrypted != null) outputStream.write(encrypted)
                bytesRead = inputStream.read(buffer)
            }
            val finalBlock = cipher.doFinal()
            if (finalBlock != null) outputStream.write(finalBlock)
        } finally {
            SecurityUtils.fillZero(buffer)
        }
    }

    override fun decrypt(
        inputStream: InputStream,
        outputStream: OutputStream,
        key: ByteArray,
        iv: ByteArray
    ) {
        val cipher = Cipher.getInstance(ALGORITHM)
        val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        val secretKey = SecretKeySpec(key, "AES")

        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val buffer = ByteArray(8192)
        try {
            var bytesRead = inputStream.read(buffer)
            while (bytesRead != -1) {
                val decrypted = cipher.update(buffer, 0, bytesRead)
                if (decrypted != null) outputStream.write(decrypted)
                bytesRead = inputStream.read(buffer)
            }
            val finalBlock = cipher.doFinal()
            if (finalBlock != null) outputStream.write(finalBlock)
        } finally {
            SecurityUtils.fillZero(buffer)
        }
    }

    override fun encryptChunked(
        inputStream: InputStream,
        outputStream: OutputStream,
        key: ByteArray,
        iv: ByteArray,
        chunkSize: Int
    ) {
        val secretKey = SecretKeySpec(key, "AES")
        val buffer = ByteArray(chunkSize)
        var chunkIndex = 0L

        try {
            var read = inputStream.read(buffer)
            while (read != -1) {
                val currentIv = incrementIV(iv, chunkIndex)
                val cipher = Cipher.getInstance(ALGORITHM)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, currentIv))
                
                val encrypted = cipher.doFinal(buffer, 0, read)
                outputStream.write(encrypted)
                
                chunkIndex++
                read = inputStream.read(buffer)
            }
        } finally {
            SecurityUtils.fillZero(buffer)
        }
    }

    override fun decryptChunked(
        inputStream: InputStream,
        outputStream: OutputStream,
        key: ByteArray,
        iv: ByteArray,
        chunkSize: Int
    ) {
        val secretKey = SecretKeySpec(key, "AES")
        val encryptedChunkSize = chunkSize + (TAG_LENGTH_BITS / 8)
        val buffer = ByteArray(encryptedChunkSize)
        var chunkIndex = 0L

        try {
            var read = inputStream.read(buffer)
            while (read != -1) {
                val currentIv = incrementIV(iv, chunkIndex)
                val cipher = Cipher.getInstance(ALGORITHM)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, currentIv))
                
                val decrypted = cipher.doFinal(buffer, 0, read)
                outputStream.write(decrypted)
                
                chunkIndex++
                read = inputStream.read(buffer)
            }
        } finally {
            SecurityUtils.fillZero(buffer)
        }
    }

    override fun decryptRange(
        inputStream: InputStream,
        outputStream: OutputStream,
        key: ByteArray,
        iv: ByteArray,
        startByte: Long,
        length: Long,
        totalSize: Long,
        chunkSize: Int
    ) {
        val secretKey = SecretKeySpec(key, "AES")
        val tagSize = TAG_LENGTH_BITS / 8
        val encryptedChunkSize = chunkSize + tagSize
        
        var currentPos = startByte
        val endByte = startByte + length
        
        while (currentPos < endByte) {
            val chunkIndex = currentPos / chunkSize
            val currentIv = incrementIV(iv, chunkIndex)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(TAG_LENGTH_BITS, currentIv))
            
            val buffer = ByteArray(encryptedChunkSize)
            val read = inputStream.read(buffer)
            if (read == -1) break
            
            val decrypted = cipher.doFinal(buffer, 0, read)
            val offsetInChunk = (currentPos % chunkSize).toInt()
            val bytesToTake = kotlin.math.min(decrypted.size - offsetInChunk, (endByte - currentPos).toInt())
            
            outputStream.write(decrypted, offsetInChunk, bytesToTake)
            
            currentPos += bytesToTake
            SecurityUtils.fillZero(decrypted)
            SecurityUtils.fillZero(buffer)
        }
    }

    private fun incrementIV(baseIv: ByteArray, counter: Long): ByteArray {
        val iv = baseIv.copyOf()
        val c = counter.toInt()
        iv[8] = (iv[8].toInt() xor (c ushr 24 and 0xFF)).toByte()
        iv[9] = (iv[9].toInt() xor (c ushr 16 and 0xFF)).toByte()
        iv[10] = (iv[10].toInt() xor (c ushr 8 and 0xFF)).toByte()
        iv[11] = (iv[11].toInt() xor (c and 0xFF)).toByte()
        return iv
    }

    override fun generateDEK(): ByteArray {
        val dek = ByteArray(KEY_SIZE_BYTES)
        SecureRandom().nextBytes(dek)
        return dek
    }

    override fun generateIV(): ByteArray {
        val iv = ByteArray(IV_LENGTH_BYTES)
        SecureRandom().nextBytes(iv)
        return iv
    }
}
