package com.sino.sdk.cloud

import java.io.File

/**
 * Generic interface for cloud storage operations.
 * Open-sourced to demonstrate how Sino interacts with third-party providers.
 */
interface CloudClient {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun resumeSession(email: String? = null): Result<Unit>
    suspend fun uploadFile(localFile: File, remotePath: String, vaultOverride: String? = null): Result<String>
    suspend fun startChunkedUpload(remotePath: String, totalSize: Long, vaultOverride: String? = null): Result<String>
    suspend fun uploadChunk(sessionId: String, chunkData: ByteArray, offset: Long, partNumber: Int, remotePath: String, vaultOverride: String? = null): Result<String>
    suspend fun finishChunkedUpload(sessionId: String, remotePath: String, etags: Map<Int, String>? = null, vaultOverride: String? = null): Result<String>
    suspend fun downloadFile(remoteFileId: String, localDestination: File, onProgress: ((Long, Long) -> Unit)? = null, vaultOverride: String? = null): Result<Unit>
    suspend fun downloadRange(remoteFileId: String, startByte: Long, length: Long, vaultOverride: String? = null): Result<ByteArray>
    suspend fun createFolder(path: String, vaultOverride: String? = null): Result<String>
    suspend fun listFiles(remotePath: String, vaultOverride: String? = null): Result<List<RemoteFile>>
    suspend fun moveToTrash(remoteFileId: String, vaultOverride: String? = null): Result<Unit>
    suspend fun restoreFromTrash(remoteFileId: String, vaultOverride: String? = null): Result<Unit>
    suspend fun deletePermanently(remoteFileId: String, vaultOverride: String? = null): Result<Unit>
    suspend fun logout(): Result<Unit>
    fun lock()
    fun clearRAM()
    fun isLoggedIn(): Boolean
    suspend fun hasResumableSession(): Boolean
    suspend fun getQuota(): Result<CloudQuota>
    suspend fun hasExistingData(): Result<Boolean>
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
