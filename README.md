# Sino Open Security SDK

Welcome to the official security core of **Sino**.

Sino is a privacy-first encryption layer designed to sit on top of your existing
cloud storage. This repository contains the reference implementation of our
**Zero-Knowledge Architecture**. By open-sourcing these core components, we
provide the community and security researchers with the means to verify our
privacy claims.

---

## 🛡️ The Sino Trust Mandate

We believe that in the realm of privacy, **if it is not verifiable, it is not
secure.**

This SDK includes the actual cryptographic engines used by the Sino application
to ensure that:

1. All encryption happens locally on your device before network transmission.
2. Only you—not Sino, not cloud providers, and not any third party—hold the keys
   to your data.
3. Cloud providers are treated as "dumb storage," remaining completely blind to
   your filenames, structures, and content.

---

## 📂 Included Components & Specifications

- **[`SPECIFICATION.md`](SPECIFICATION.md)**: Formal open specification for
  Sino's 4-tier key hierarchy, path salting, and 1MB chunked GCM streaming.
- **`crypto/AESEncryptionEngine.kt`**: Implementation of AES-256-GCM, including
  our specialized **Chunked GCM** logic for secure, zero-latency media
  streaming.
- **`crypto/SinoKeyDerivation.kt`**: Implementation of **Argon2id** (64MB RAM, 3
  iterations, 4 parallelism threads) for high-entropy key derivation.
- **`crypto/CloudPathHasher.kt`**: HMAC-SHA256 path anonymizer ensuring complete
  "Cloud Blindness" (zero filename/folder structure leak to providers).
- **`crypto/DuressKeyDerivation.kt`**: Domain-separated key derivation
  specification for dual-vault decoy key isolation.
- **`cli/SinoDecryptorCLI.kt`**: Standalone desktop CLI recovery runner (runs on
  Linux/macOS/Windows independently of the mobile app).
- **`cloud/CloudClient.kt`**: Interface defining our "Blind Cloud" protocol.
- **`models/MetadataModel.kt`**: Specification for Sino's encrypted metadata
  blobs.

---

## 🚀 Usage Instructions

### 1. Building the SDK Jar & Running Tests

To compile the SDK and execute the unit test suite on any platform (Linux,
macOS, Windows):

```bash
# Run unit test suite
./gradlew test

# Package the standalone JAR artifact
./gradlew jar
```

### 2. Standalone Desktop File Recovery (CLI)

Users can recover and decrypt their Sino files on any desktop operating system
without needing an Android device or the mobile application binary:

```bash
# Syntax
java -cp build/libs/sino-open-sdk-1.0.0.jar com.sino.sdk.cli.SinoDecryptorCLI <input_encrypted_file> <output_decrypted_file> <base64_dek> <base64_iv>

# Example
java -cp build/libs/sino-open-sdk-1.0.0.jar com.sino.sdk.cli.SinoDecryptorCLI encrypted_payload.sino restored_photo.jpg K7aB...== Iv9x...==
```

### 3. Programmatic Integration Examples (Kotlin / Java)

#### A. AES-256-GCM Stream Encryption & Decryption

```kotlin
import com.sino.sdk.crypto.AESEncryptionEngine
import java.security.SecureRandom

val engine = AESEncryptionEngine()
val dek = ByteArray(32).also { SecureRandom().nextBytes(it) }
val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }

// Encrypt payload stream
engine.encrypt(inputStream, outputStream, dek, iv)

// Decrypt payload stream
engine.decrypt(encryptedInputStream, decryptedOutputStream, dek, iv)
```

#### B. Argon2id Key Derivation

```kotlin
import com.sino.sdk.crypto.SinoKeyDerivation

val derivation = SinoKeyDerivation()
val salt = "UserSpecificSalt123".toByteArray()
val derivedKey = derivation.deriveKey("MasterPassword123!", salt)
```

#### C. Cloud Path Anonymization (HMAC-SHA256)

```kotlin
import com.sino.sdk.crypto.CloudPathHasher

val pathSalt = "SecretPathSaltBytes".toByteArray()
val opaqueKey = CloudPathHasher.hashPath("DCIM/Vacation/family.jpg", pathSalt)
// Returns opaque hex string: "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
```

---

## 🤖 AI-Augmented Security Auditing

The development of this SDK and the broader Sino architecture involved the use
of **Advanced Artificial Intelligence (AI)** as a continuous security auditor.

Throughout the implementation phase, AI was utilized to:

- **Resilience Testing**: Perform real-time analysis of cryptographic
  implementations to ensure adherence to military-grade standards.
- **Memory Hygiene**: Verify that RAM sanitization logic (e.g.,
  `SecurityUtils.fillZero`) is consistently applied to prevent forensic data
  leakage.
- **Pattern Validation**: Audit the "Cloud Blindness" protocols to ensure no
  metadata or structural identifiers are leaked to third-party storage
  providers.

By combining human architectural design with AI-driven auditing, we have created
a hardened foundation for personal digital sovereignty.

---

## ⚖️ License & Contributions

This SDK is released under the MIT License. We welcome peer reviews and security
audits.

---

_For more information on the Sino project, visit
[Sino Privacy](https://sinosecure.app/privacy)._
