package com.sino.sdk.crypto

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Open Specification for Sino's Cloud Path Anonymization.
 * Converts readable folder paths (e.g. "DCIM/Vacation/photo.jpg") into opaque,
 * deterministic hashes using HMAC-SHA256 with a salt to ensure complete "Cloud Blindness".
 */
class CloudPathHasher {

    companion object {
        private const val HMAC_ALGORITHM = "HmacSHA256"

        /**
         * Computes an opaque, deterministic cloud filename/path.
         *
         * @param relativePath The original relative path (e.g., "Documents/Taxes.pdf")
         * @param pathSalt High-entropy salt derived from the user's master key
         * @return Opaque hex string suitable for use as a remote object key
         */
        fun hashPath(relativePath: String, pathSalt: ByteArray): String {
            val normalizedPath = relativePath.trim().replace('\\', '/')
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            val keySpec = SecretKeySpec(pathSalt, HMAC_ALGORITHM)
            mac.init(keySpec)

            val hashBytes = mac.doFinal(normalizedPath.toByteArray(Charsets.UTF_8))
            return bytesToHex(hashBytes)
        }

        /**
         * Fast SHA-256 fallback for zero-knowledge path hashing when salt is omitted.
         */
        fun hashPathSha256(relativePath: String): String {
            val normalizedPath = relativePath.trim().replace('\\', '/')
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(normalizedPath.toByteArray(Charsets.UTF_8))
            return bytesToHex(hashBytes)
        }

        private fun bytesToHex(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            val hexArray = "0123456789abcdef".toCharArray()
            for (i in bytes.indices) {
                val v = bytes[i].toInt() and 0xFF
                hexChars[i * 2] = hexArray[v ushr 4]
                hexChars[i * 2 + 1] = hexArray[v and 0x0F]
            }
            return String(hexChars)
        }
    }
}
