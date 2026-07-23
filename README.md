# Sino Open Security SDK

Welcome to the official security core of **Sino**.

Sino is a privacy-first encryption layer designed to sit on top of your existing
cloud storage. This repository contains the reference implementation of our
**Zero-Knowledge Architecture**. By open-sourcing these core components, we
provide the community and security researchers with the means to verify our
privacy claims.

## 🛡️ The Sino Trust Mandate

We believe that in the realm of privacy, **if it is not verifiable, it is not
secure.**

This SDK includes the actual cryptographic engines used by the Sino Android
application to ensure that:

1. All encryption happens locally on your device.
2. Only you not Sino, not the cloud provider, and not any third party hold the
   keys to your data.
3. Cloud providers are treated as "dumb storage," remaining completely blind to
   your filenames, structures, and content.

## 📂 Included Components & Specifications

- **[`SPECIFICATION.md`](SPECIFICATION.md)**: Formal open specification for Sino's 4-tier key hierarchy, path salting, and 1MB chunked GCM streaming.
- **`crypto/AESEncryptionEngine.kt`**: Implementation of AES-256-GCM, including our specialized **Chunked GCM** logic for secure, zero-latency media streaming.
- **`crypto/SinoKeyDerivation.kt`**: Implementation of **Argon2id** (64MB RAM, 3 iterations, 4 parallelism threads) for high-entropy key derivation.
- **`crypto/CloudPathHasher.kt`**: HMAC-SHA256 path anonymizer ensuring complete "Cloud Blindness" (zero filename/folder structure leak to providers).
- **`crypto/DuressKeyDerivation.kt`**: Domain-separated key derivation specification for dual-vault decoy key isolation.
- **`cli/SinoDecryptorCLI.kt`**: Standalone desktop CLI recovery runner (runs on Linux/macOS/Windows independently of the mobile app).
- **`cloud/CloudClient.kt`**: Interface defining our "Blind Cloud" protocol.
- **`models/MetadataModel.kt`**: Specification for Sino's encrypted metadata blobs.

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

## ⚖️ License & Contributions

This SDK is released under the MIT License. We welcome peer reviews and
suggestions to further harden the Sino ecosystem.

---

_For more information on the Sino project, visit
[Sino Privacy](https://sinosecure.app/privacy)._
