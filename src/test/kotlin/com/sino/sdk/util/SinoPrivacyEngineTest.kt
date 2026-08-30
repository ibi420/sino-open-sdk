package com.sino.sdk.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SinoPrivacyEngineTest {

    private val engine = SinoPrivacyEngine()

    @Test
    fun `sanitize redacts emails`() {
        val input = "Error for user@domain.com"
        val expected = "Error for [REDACTED_EMAIL]"
        assertEquals(expected, engine.sanitize(input))
    }

    @Test
    fun `sanitize redacts BIP39 phrases`() {
        val input = "Seed apple banana cherry dog elephant fish grape horse ice jacket kangaroo lemon"
        val expected = "Seed [REDACTED_BIP39]"
        assertEquals(expected, engine.sanitize(input))
    }

    @Test
    fun `sanitize redacts multiple sensitive items in single pass`() {
        val input = "User dev@sino.com used token ya29.auth123 to decrypt photo.jpg"
        val expected = "User [REDACTED_EMAIL] used token [REDACTED_AUTH_TOKEN] to decrypt [REDACTED_FILE_NAME]"
        assertEquals(expected, engine.sanitize(input))
    }

    @Test
    fun `sanitize handles Base64 blobs`() {
        // SGVsbG8gV29ybGQgZnJvbSBTaW5vIFNlY3VyZSBVbml2ZXJzZSAyMDI2IQ== is 56 chars
        // The regex threshold is {15,} blocks of 4 = 60 chars.
        val longBase64 = "SGVsbG8gV29ybGQgZnJvbSBTaW5vIFNlY3VyZSBVbml2ZXJzZSAyMDI2ISBXZSBhcmUgaGFyZGVuaW5nIHRoZSBlbmdpbmUgdG8gUHJvdG9jb2wgdjMuMi4="
        val input = "Payload: $longBase64"
        val expected = "Payload: [REDACTED_BASE64]"
        assertEquals(expected, engine.sanitize(input))
    }
}
