package com.sino.sdk.cloud

import java.io.File

/**
 * Generic interface for cloud storage operations.
 * Open-sourced to demonstrate how Sino interacts with third-party providers.
 */
interface CloudClient {
    suspend fun login(email: String, password: String): Result<Unit>
    suspend fun resumeSession(email: String? = null): Result<Unit>
    suspend fun uploadFile(localFile: File, remotePath: String): Result<String>
    suspend fun startChunkedUpload(remotePath: String, totalSize: Long): Result<String>
    suspend fun uploadChunk(sessionId: String, chunkData: ByteArray, offset: Long): Result<Unit>
    suspend fun finishChunkedUpload(sessionId: String): Result<String>
    suspend fun downloadFile(remoteFileId: String, localDestination: File, onProgress: ((Long, Long) -> Unit)? = null): Result<Unit>
    suspend fun downloadRange(remoteFileId: String, startByte: Long, length: Long): Result<ByteArray>
    suspend fun createFolder(path: String): Result<String>
    suspend fun listFiles(remotePath: String): Result<List<RemoteFile>>
    suspend fun moveToTrash(remoteFileId: String): Result<Unit>
    suspend fun restoreFromTrash(remoteFileId: String): Result<Unit>
    suspend fun deletePermanently(remoteFileId: String): Result<Unit>
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
