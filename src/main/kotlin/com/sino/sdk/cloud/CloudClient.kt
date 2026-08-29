package com.sino.sdk.cloud

/**
 * Generic interface for cloud storage operations.
 * Open-sourced to demonstrate how Sino interacts with third-party providers.
 * Hardened v3: Aligned with production RAID protocol for full lifecycle management.
 */
interface CloudClient {
    suspend fun login(email: String, password: String, mfaCode: String? = null): Result<Unit>
    suspend fun resumeSession(email: String? = null): Result<Unit>
    
    fun setSessionExpiredListener(listener: () -> Unit)

    // Using specialized Sino artifacts for SDK parity
    suspend fun uploadFile(source: okio.Source, remotePath: String, size: Long, vaultOverride: String? = null): Result<String>
    
    suspend fun startChunkedUpload(remotePath: String, totalSize: Long, vaultOverride: String? = null): Result<String>
    suspend fun uploadChunk(sessionId: String, chunkData: ByteArray, offset: Long, partNumber: Int, remotePath: String, vaultOverride: String? = null): Result<String>
    suspend fun finishChunkedUpload(sessionId: String, remotePath: String, etags: Map<Int, String>? = null, vaultOverride: String? = null): Result<String>
    
    suspend fun downloadFile(remoteFileId: String, sink: okio.Sink, onProgress: ((Long, Long) -> Unit)? = null, vaultOverride: String? = null): Result<Unit>
    suspend fun downloadRange(remoteFileId: String, startByte: Long, length: Long, vaultOverride: String? = null): Result<ByteArray>
    
    suspend fun createFolder(path: String, vaultOverride: String? = null): Result<String>
    suspend fun listFiles(remotePath: String, vaultOverride: String? = null): Result<List<RemoteFile>>
    suspend fun renameFolder(oldName: String, newName: String, vaultOverride: String? = null): Result<Unit>
    suspend fun renameVault(newName: String): Result<Unit>
    
    // ELITE: Vault Archival & Lifecycle
    suspend fun findVaultBackups(): Result<List<RemoteFile>>
    suspend fun deleteVaultBackup(name: String): Result<Unit>
    
    suspend fun moveToTrash(remoteFileId: String, vaultOverride: String? = null): Result<Unit>
    suspend fun restoreFromTrash(remoteFileId: String, vaultOverride: String? = null): Result<Unit>
    suspend fun deletePermanently(remoteFileId: String, vaultOverride: String? = null): Result<Unit>
    
    suspend fun logout(): Result<Unit>
    suspend fun isLoggedIn(): Boolean
    
    /**
     * Checks if there is a saved session in the secure vault that can be resumed.
     */
    suspend fun hasResumableSession(): Boolean

    /**
     * Retrieves the current cloud storage quota.
     */
    suspend fun getQuota(vaultOverride: String? = null): Result<CloudQuota>

    /**
     * Checks for the presence of existing Sino data in the cloud.
     */
    suspend fun hasExistingData(): Result<Boolean>

    /**
     * Permanently deletes any hidden application-specific storage.
     */
    suspend fun deleteHiddenStorage(): Result<Unit>

    /**
     * Permanently deletes the visible application folder.
     */
    suspend fun deleteVisibleStorage(): Result<Unit>

    /**
     * Standard memory hygiene for sensitive session material.
     */
    suspend fun clearMemoryKeys()
}

data class RemoteFile(
    val name: String,
    val id: String,
    val isDirectory: Boolean,
    val fullPath: String
)

data class CloudQuota(
    val usedBytes: Long,
    val totalBytes: Long
)
