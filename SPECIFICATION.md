# Sino Cryptographic Specification & Architecture

This document provides the formal open specification for **Sino's Zero-Knowledge Encryption Layer**.

---

## 1. Cryptographic Key Hierarchy

Sino enforces a 4-tier cryptographic key hierarchy to ensure local encryption and RAM isolation:

```
[ User Password / PIN ]
          │
          ▼  (Argon2id - 64MB RAM, 3 iterations, 4 parallelism)
[ Password Hash / Key ]
          │
          ▼  (AES-256-GCM Unwrap)
[ Root Master Key (RMK) ]
          │
          ▼  (HKDF-HMAC-SHA256 Domain Separation)
[ Domain-Specific Key ] (e.g., Auth, Sync, Metadata)
          │
          ▼  (AES-256-GCM Key Wrapping)
[ Data Encryption Key (DEK) ] ──▶ Encrypts Payload Bytes
```

---

## 2. Encryption Engine Specification (Protocol v3)

- **Algorithm**: AES-256-GCM (`AES/GCM/NoPadding`)
- **Key Length**: 256 bits (32 bytes)
- **IV Length**: 96 bits (12 bytes)
- **Authentication Tag Length**: 128 bits (16 bytes)

### 2.1 Chunked GCM Streaming Specification (Encryption v2)
For large files and direct cloud media streaming:
1. Payloads are divided into **1MB fixed chunks** (1,048,576 bytes).
2. Each chunk is encrypted as an independent GCM packet (Plaintext + 16-byte Tag).
3. **Nonce Derivation (v2)**: The last 4 bytes of the base 96-bit IV are overwritten with the 32-bit big-endian chunk counter.
   $$\text{IV}_{\text{chunk}} = \text{BaseIV}[0..7] \parallel \text{BigEndian32}(\text{ChunkIndex})$$
4. **Legacy Support (v1)**: Older versions used XOR derivation ($\text{BaseIV} \oplus \text{BigEndian32}(\text{ChunkIndex})$). Modern Sino engines detect the version from metadata and maintain bit-perfect compatibility.

---

## 3. Cloud Blindness & RAID Path Anonymization

Sino converts human-readable filenames and relative folder paths into opaque, un-linkable cloud object keys using **Salted HMAC-SHA256**. To ensure forensic privacy and filesystem compatibility, these hashes are truncated according to the **RAID Discovery Standard**:

### 3.1 Standard Truncation Lengths
- **Cloud Folders**: HMAC-SHA256 hash of the relative path, truncated to **16 characters** (hex).
- **Deterministic Filenames**: HMAC-SHA256 hash of the file checksum, truncated to **16 characters** (hex).
- **Metadata Batch Names**: HMAC-SHA256 hash of the logical identifier, truncated to **12 characters** (hex) with a `.batch` suffix.

### 3.2 Dictionary Attack Defense
The use of a high-entropy, hardware-wrapped **Vault Salt** ensures that cloud providers cannot perform dictionary attacks (pre-computing hashes of common filenames) to identify user data.

---

## 4. Technical Artifact Wrapping (v2 Standard)

All system-level artifacts (Metadata batches, Snapshots, Backups) are enclosed in an authenticated container to prevent cloud-side tampering or bit-rot:

**[ MAGIC (4B) ] [ VERSION (1B) ] [ IV (12B) ] [ ENCRYPTED_DATA (NB) ] [ SHA256_CHECKSUM (32B) ]**

1. **Magic**: Constant `SINO`.
2. **Version**: Current standard is `2`.
3. **Integrity**: The trailing 32-byte SHA-256 hash covers the entire packet from MAGIC to the end of the ciphertext. Any artifact failing this check is rejected as "Forensically Compromised."

---

## 5. Dual-Vault Duress Isolation Specification

- Primary Vault and Decoy (Duress) Vault use **domain-separated Argon2id salts**:
  - `SinoPrimaryMasterKeySaltV1` for Primary Vault
  - `SinoDuressDecoyKeySaltV1` for Decoy Vault
- Primary keys and Decoy keys share zero mathematical linkage. Analyzing the algorithm or code structure gives zero cryptographic indicator of whether a secondary decoy vault exists.

---

## 6. Memory Hygiene Specification

All transient sensitive byte arrays (`ByteArray` representing DEK, RMK, or plaintexts) are explicitly zeroed out in `finally` blocks using `SecurityUtils.fillZero(array)` to prevent data extraction from memory dumps.

---

## 7. Metadata Specification

Sino utilizes a structured JSON format for file metadata. This metadata is the "Source of Truth" for the RAID engine.

### 7.1 Data Fields
- `originalName`: The original filename (including extension).
- `relativePath`: The logical directory structure.
- `mimeType`: Technical file classification.
- `size`: Original file size in bytes.
- `checksum`: SHA-256 fingerprint of the original plaintext.
- `cloudChecksum`: SHA-256 fingerprint of the final optimized/transcoded blob.
- `encryptionVersion`: Format version (1 = XOR nonce, 2 = Counter nonce).
- `encryptedDEK`: The file's unique 256-bit AES key, wrapped by the RMK.
- `iv`: The base 96-bit Initialization Vector.
- `isCompressed`: Gzip optimization flag.
- `isChunked`: supports 1MB GCM chunked random access.
- `isDuress`: Forensic isolation flag.
- `providerHints`: A list of RAID targets (e.g., `["MEGA", "GOOGLE_DRIVE", "S3:100"]`).
