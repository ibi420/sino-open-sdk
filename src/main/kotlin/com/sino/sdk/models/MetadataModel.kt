package com.sino.sdk.models

/**
 * Open Specification for Sino's Encrypted Metadata.
 * This defines the standard format for file identification and key mapping.
 */
data class SinoMetadata(
    val originalName: String,
    val relativePath: String,
    val mimeType: String,
    val size: Long,
    val checksum: String, // Original file SHA-256
    val cloudChecksum: String?, // Post-transcode SHA-256
    val timestamp: Long,
    
    // Cryptographic Binding
    val encryptedDEK: String, // The file key, wrapped by the Root Master Key (RMK)
    val iv: String, // The base Initialization Vector
    
    // Performance Flags
    val isCompressed: Boolean,
    val isChunked: Boolean, // Supports 1MB GCM chunked random access
    val isDuress: Boolean // Forensic isolation flag
)
