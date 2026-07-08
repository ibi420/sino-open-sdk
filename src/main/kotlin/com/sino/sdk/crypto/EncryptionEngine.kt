package com.sino.sdk.crypto

import java.io.InputStream
import java.io.OutputStream

/**
 * Interface for Encryption Engine and Key Management.
 */
interface EncryptionEngine {
    fun generateDEK(): ByteArray
    fun generateIV(): ByteArray
    fun encrypt(inputStream: InputStream, outputStream: OutputStream, key: ByteArray, iv: ByteArray)
    fun decrypt(inputStream: InputStream, outputStream: OutputStream, key: ByteArray, iv: ByteArray)

    // ELITE: Chunked GCM for Direct Cloud Streaming
    fun encryptChunked(inputStream: InputStream, outputStream: OutputStream, key: ByteArray, iv: ByteArray, chunkSize: Int = 1024 * 1024)
    fun decryptChunked(inputStream: InputStream, outputStream: OutputStream, key: ByteArray, iv: ByteArray, chunkSize: Int = 1024 * 1024)

    // ELITE: Partial Decryption (Random Access)
    fun decryptRange(inputStream: InputStream, outputStream: OutputStream, key: ByteArray, iv: ByteArray, startByte: Long, length: Long, totalSize: Long, chunkSize: Int = 1024 * 1024)
}
