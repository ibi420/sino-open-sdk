package com.sino.sdk.crypto

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityUtilsTest {

    @Test
    fun `fillZero physically wipes byte array`() {
        val data = byteArrayOf(1, 2, 3, 4, 5)
        SecurityUtils.fillZero(data)
        assertTrue(data.all { it == 0.toByte() })
    }

    @Test
    fun `fillZero physically wipes char array`() {
        val data = charArrayOf('a', 'b', 'c')
        SecurityUtils.fillZero(data)
        assertTrue(data.all { it == '\u0000' })
    }

    @Test
    fun `toUtf8 and fromUtf8 are reversible for complex strings`() {
        val original = "Elite Privacy: \u00a9 2026 \u20ac".toCharArray() // Copyright and Euro symbols
        val bytes = SecurityUtils.toUtf8(original)
        val recovered = SecurityUtils.fromUtf8(bytes)
        
        assertArrayEquals("UTF-8 round-trip failed for complex characters", original, recovered)
    }

    @Test
    fun `toUtf8 handles empty arrays`() {
        val original = charArrayOf()
        val bytes = SecurityUtils.toUtf8(original)
        assertTrue(bytes.isEmpty())
    }

    @Test
    fun `fromUtf8 handles empty arrays`() {
        val bytes = byteArrayOf()
        val recovered = SecurityUtils.fromUtf8(bytes)
        assertTrue(recovered.isEmpty())
    }

    @Test
    fun `toUtf8 and fromUtf8 handle supplementary characters`() {
        // "Sino 🔒" - contains the locked padlock emoji (4-byte UTF-8, U+1F512)
        val original = "Sino \ud83d\udd12".toCharArray()
        val bytes = SecurityUtils.toUtf8(original)
        
        // U+1F512 in UTF-8 is F0 9F 94 92
        org.junit.Assert.assertEquals(9, bytes.size)
        org.junit.Assert.assertEquals(0xF0.toByte(), bytes[5])
        
        val recovered = SecurityUtils.fromUtf8(bytes)
        assertArrayEquals("Emoji round-trip failed", original, recovered)
    }

    @Test
    fun `requestSystemGc can be called safely`() {
        // Smoke test: ensures no exceptions when hinting GC
        SecurityUtils.requestSystemGc()
    }

    @Test
    fun `toHex and fromHex are reversible`() {
        val original = byteArrayOf(0x00, 0x01, 0x7F, 0x80.toByte(), 0xFF.toByte())
        val hex = SecurityUtils.toHex(original)
        
        org.junit.Assert.assertEquals("00017f80ff", String(hex))
        
        val recovered = SecurityUtils.fromHex(hex)
        assertArrayEquals(original, recovered)
    }

    @Test
    fun `toBase64 and fromBase64 are reversible`() {
        val original = "Sino Open SDK Forensic Verification".toByteArray()
        
        // 1. Standard
        val b64 = SecurityUtils.toBase64(original, urlSafe = false)
        val recovered = SecurityUtils.fromBase64(b64, urlSafe = false)
        assertArrayEquals("Standard Base64 failed", original, recovered)
        
        // 2. URL Safe
        val b64Url = SecurityUtils.toBase64(original, urlSafe = true)
        val recoveredUrl = SecurityUtils.fromBase64(b64Url, urlSafe = true)
        assertArrayEquals("URL Safe Base64 failed", original, recoveredUrl)
    }

    @Test
    fun `contentEquals is timing-attack resistant but accurate`() {
        val a = "token_123".toCharArray()
        val b = "token_123".toCharArray()
        val c = "token_456".toCharArray()

        assertTrue(SecurityUtils.contentEquals(a, b))
        assertTrue(SecurityUtils.contentEquals(null, null))
        assertTrue(!SecurityUtils.contentEquals(a, c))
        assertTrue(!SecurityUtils.contentEquals(a, null))
    }
}
