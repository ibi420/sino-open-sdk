package com.sino.sdk.cli

import com.sino.sdk.crypto.AESEncryptionEngine
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Base64

/**
 * Standalone Desktop CLI Decryptor for Sino Encrypted Blobs.
 * Allows privacy auditors and users to recover their encrypted files on any
 * Linux/macOS/Windows desktop environment independently of the mobile application.
 *
 * Usage:
 *   java -cp sino-open-sdk.jar com.sino.sdk.cli.SinoDecryptorCLI <input_encrypted_file> <output_file> <base64_dek> <base64_iv> [is_chunked]
 */
object SinoDecryptorCLI {

    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Sino Open Security SDK - Decryptor CLI ===")

        if (args.size < 4) {
            println("Error: Insufficient arguments.")
            println("Usage: java -cp sino-open-sdk.jar com.sino.sdk.cli.SinoDecryptorCLI <input_file> <output_file> <base64_dek> <base64_iv> [is_chunked] [encryption_version]")
            return
        }

        val inputFilePath = args[0]
        val outputFilePath = args[1]
        val base64Dek = args[2]
        val base64Iv = args[3]
        val isChunked = args.getOrNull(4)?.toBoolean() ?: true // Default to true as per Phase 9+
        val encryptionVersion = args.getOrNull(5)?.toIntOrNull() ?: 1

        val inputFile = File(inputFilePath)
        val outputFile = File(outputFilePath)

        if (!inputFile.exists()) {
            println("Error: Input file does not exist: $inputFilePath")
            return
        }

        try {
            val dek = Base64.getDecoder().decode(base64Dek)
            val iv = Base64.getDecoder().decode(base64Iv)

            val engine = AESEncryptionEngine()

            println("Decrypting [${inputFile.name}] -> [${outputFile.name}] (Chunked: $isChunked, Version: $encryptionVersion)...")
            FileInputStream(inputFile).use { inStream ->
                FileOutputStream(outputFile).use { outStream ->
                    if (isChunked) {
                        engine.decryptChunked(inStream, outStream, dek, iv, 1024 * 1024, encryptionVersion)
                    } else {
                        engine.decrypt(inStream, outStream, dek, iv)
                    }
                }
            }

            println("SUCCESS: File decrypted cleanly (${outputFile.length()} bytes). Integrity verified.")
        } catch (e: Exception) {
            println("DECRYPTION FAILED: ${e.message}")
            e.printStackTrace()
        }
    }
}
