package com.sino.sdk.crypto

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Open Specification for Sino's Cloud Path Anonymization.
 * Converts readable folder paths (e.g. "DCIM/Vacation/photo.jpg") into opaque,
 * deterministic hashes using HMAC-SHA256 with a salt to ensure complete "Cloud Blindness".
 * Hardened v3: Implements standard RAID truncation for discovery parity.
 */
class CloudPathHasher {

    companion object {
        private const val HMAC_ALGORITHM = "HmacSHA256"
        
        /**
         * Standard truncation length for anonymized cloud folders and deterministic filenames.
         */
        const val PATH_TRUNCATION_LENGTH = 16

        /**
         * Standard truncation length for metadata batch identifiers.
         */
        const val BATCH_TRUNCATION_LENGTH = 12

        /**
         * Computes an opaque, deterministic cloud folder hash.
         * Used for constructing remote directory structures.
         */
        fun computeCloudFolderHash(fullRelativePath: String, pathSalt: ByteArray): String {
            return hashPath(fullRelativePath, pathSalt).substring(0, PATH_TRUNCATION_LENGTH)
        }

        /**
         * Computes an opaque, deterministic cloud filename.
         * Used for content-addressable storage (Deduplication).
         */
        fun computeCloudFileHash(fileChecksum: String, pathSalt: ByteArray): String {
            return hashPath(fileChecksum, pathSalt).substring(0, PATH_TRUNCATION_LENGTH)
        }

        /**
         * Computes the obfuscated name for a metadata batch file.
         */
        fun computeMetadataBatchHash(folderId: Long, pathSalt: ByteArray): String {
            val base = "folder_metadata_$folderId"
            val hash = hashPath(base, pathSalt).substring(0, BATCH_TRUNCATION_LENGTH)
            return "$hash.batch"
        }

        /**
         * Internal helper to compute raw 64-character HMAC-SHA256 hash.
         */
        private fun hashPath(input: String, pathSalt: ByteArray): String {
            val normalized = input.trim().replace('\\', '/')
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            val keySpec = SecretKeySpec(pathSalt, HMAC_ALGORITHM)
            mac.init(keySpec)

            val hashBytes = mac.doFinal(normalized.toByteArray(Charsets.UTF_8))
            val hexChars = SecurityUtils.toHex(hashBytes)
            val result = hexChars.concatToString()
            SecurityUtils.fillZero(hexChars)
            return result
        }
    }
}
