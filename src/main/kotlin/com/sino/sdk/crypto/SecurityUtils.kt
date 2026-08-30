package com.sino.sdk.crypto

import java.util.Arrays

/**
 * Security utilities for hardening memory against data extraction.
 */
object SecurityUtils {

    /**
     * Overwrites a ByteArray with zeros. 
     * Use this immediately after a sensitive key is no longer needed.
     */
    fun fillZero(array: ByteArray?) {
        if (array == null) return
        Arrays.fill(array, 0.toByte())
    }

    /**
     * Overwrites a CharArray with zeros.
     * Use this immediately after a sensitive password is no longer needed.
     */
    fun fillZero(array: CharArray?) {
        if (array == null) return
        Arrays.fill(array, '\u0000')
    }

    /**
     * Converts a CharArray to a UTF-8 encoded ByteArray without creating a transient String.
     * ELITE: Ensures no immutable String "ghosts" remain in memory.
     * Supports supplementary planes (e.g. emojis).
     */
    fun toUtf8(chars: CharArray): ByteArray {
        var len = 0
        var i = 0
        while (i < chars.size) {
            val c1 = chars[i]
            if (c1.isHighSurrogate() && i + 1 < chars.size && chars[i+1].isLowSurrogate()) {
                len += 4
                i += 2
            } else {
                val code = c1.code
                len += when {
                    code < 0x80 -> 1
                    code < 0x800 -> 2
                    else -> 3
                }
                i += 1
            }
        }

        val result = ByteArray(len)
        var pos = 0
        i = 0
        while (i < chars.size) {
            val c1 = chars[i]
            if (c1.isHighSurrogate() && i + 1 < chars.size && chars[i+1].isLowSurrogate()) {
                val c2 = chars[i + 1]
                val codePoint = 0x10000 + ((c1.code and 0x3FF) shl 10) or (c2.code and 0x3FF)
                result[pos++] = (0xf0 or (codePoint shr 18)).toByte()
                result[pos++] = (0x80 or ((codePoint shr 12) and 0x3f)).toByte()
                result[pos++] = (0x80 or ((codePoint shr 6) and 0x3f)).toByte()
                result[pos++] = (0x80 or (codePoint and 0x3f)).toByte()
                i += 2
            } else {
                val code = c1.code
                when {
                    code < 0x80 -> result[pos++] = code.toByte()
                    code < 0x800 -> {
                        result[pos++] = (0xc0 or (code shr 6)).toByte()
                        result[pos++] = (0x80 or (code and 0x3f)).toByte()
                    }
                    else -> {
                        result[pos++] = (0xe0 or (code shr 12)).toByte()
                        result[pos++] = (0x80 or ((code shr 6) and 0x3f)).toByte()
                        result[pos++] = (0x80 or (code and 0x3f)).toByte()
                    }
                }
                i += 1
            }
        }
        return result
    }

    /**
     * Converts a UTF-8 encoded ByteArray to a CharArray without creating a transient String.
     * ELITE: Forensically isolated decoding. Supports supplementary planes (e.g. emojis).
     */
    fun fromUtf8(bytes: ByteArray): CharArray {
        var charCount = 0
        var i = 0
        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xFF
            if (b < 0x80) {
                charCount++
                i += 1
            } else if (b < 0xE0) {
                charCount++
                i += 2
            } else if (b < 0xF0) {
                charCount++
                i += 3
            } else {
                // 4-byte UTF-8 sequence -> Surrogate pair in UTF-16 (2 chars)
                charCount += 2
                i += 4
            }
        }

        val result = CharArray(charCount)
        var pos = 0
        i = 0
        while (i < bytes.size) {
            val b1 = bytes[i].toInt() and 0xFF
            if (b1 < 0x80) {
                result[pos++] = b1.toChar()
                i += 1
            } else if (b1 < 0xE0) {
                val b2 = bytes[i + 1].toInt() and 0x3F
                result[pos++] = (((b1 and 0x1F) shl 6) or b2).toChar()
                i += 2
            } else if (b1 < 0xF0) {
                val b2 = bytes[i + 1].toInt() and 0x3F
                val b3 = bytes[i + 2].toInt() and 0x3F
                result[pos++] = (((b1 and 0x0F) shl 12) or (b2 shl 6) or b3).toChar()
                i += 3
            } else {
                val b2 = bytes[i + 1].toInt() and 0x3F
                val b3 = bytes[i + 2].toInt() and 0x3F
                val b4 = bytes[i + 3].toInt() and 0x3F
                // Handle 4-byte UTF-8 (Supplementary planes)
                val codePoint = ((b1 and 0x07) shl 18) or (b2 shl 12) or (b3 shl 6) or b4
                
                // Convert to surrogate pair
                val shifted = codePoint - 0x10000
                result[pos++] = (0xD800 or (shifted shr 10)).toChar()
                result[pos++] = (0xDC00 or (shifted and 0x3FF)).toChar()
                i += 4
            }
        }
        return result
    }

    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    /**
     * Converts a ByteArray to a Hex CharArray without creating a transient String.
     */
    fun toHex(data: ByteArray): CharArray {
        val result = CharArray(data.size * 2)
        for (i in data.indices) {
            val v = data[i].toInt() and 0xFF
            result[i * 2] = HEX_CHARS[v ushr 4]
            result[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return result
    }

    /**
     * Converts a Hex CharArray back to a ByteArray without creating a transient String.
     */
    fun fromHex(chars: CharArray): ByteArray {
        require(chars.size % 2 == 0) { "Hex string must have an even length" }
        val result = ByteArray(chars.size / 2)
        for (i in result.indices) {
            val h = parseHexDigit(chars[i * 2])
            val l = parseHexDigit(chars[i * 2 + 1])
            result[i] = ((h shl 4) or l).toByte()
        }
        return result
    }

    private fun parseHexDigit(c: Char): Int = when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + 10
        in 'A'..'F' -> c - 'A' + 10
        else -> throw IllegalArgumentException("Invalid hex digit: $c")
    }

    private val B64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray()
    private val B64_URL_SAFE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray()

    /**
     * Converts a ByteArray to a Base64 CharArray without creating a transient String.
     */
    fun toBase64(data: ByteArray, urlSafe: Boolean = false, pad: Boolean = true): CharArray {
        val alphabet = if (urlSafe) B64_URL_SAFE_CHARS else B64_CHARS
        val resultSize = if (pad) (data.size + 2) / 3 * 4 else (data.size * 8 + 5) / 6
        val result = CharArray(resultSize)
        var i = 0
        var r = 0
        while (i < data.size) {
            val b0 = data[i++].toInt() and 0xFF
            val b1 = if (i < data.size) data[i++].toInt() and 0xFF else -1
            val b2 = if (i < data.size) data[i++].toInt() and 0xFF else -1

            result[r++] = alphabet[b0 ushr 2]
            if (b1 != -1) {
                result[r++] = alphabet[(b0 and 0x03 shl 4) or (b1 ushr 4)]
                if (b2 != -1) {
                    result[r++] = alphabet[(b1 and 0x0F shl 2) or (b2 ushr 6)]
                    result[r++] = alphabet[b2 and 0x3F]
                } else {
                    result[r++] = alphabet[b1 and 0x0F shl 2]
                    if (pad) result[r++] = '='
                }
            } else {
                result[r++] = alphabet[b0 and 0x03 shl 4]
                if (pad) {
                    result[r++] = '='
                    result[r++] = '='
                }
            }
        }
        return result
    }

    /**
     * Converts a Base64 CharArray to a ByteArray without creating a transient String.
     */
    fun fromBase64(chars: CharArray, urlSafe: Boolean = false): ByteArray {
        val alphabet = if (urlSafe) B64_URL_SAFE_CHARS else B64_CHARS
        val inv = IntArray(256) { -1 }
        for (i in alphabet.indices) inv[alphabet[i].code] = i
        
        var actualLen = 0
        for (c in chars) {
            if (c == '=' || c.isWhitespace()) continue
            actualLen++
        }

        val result = ByteArray(actualLen * 3 / 4)
        var i = 0
        var r = 0
        var bits = 0
        var buffer = 0
        while (i < chars.size) {
            val c = chars[i++]
            if (c.isWhitespace() || c == '=') continue
            val v = inv[c.code]
            if (v == -1) throw IllegalArgumentException("Invalid Base64 character: $c")
            buffer = (buffer shl 6) or v
            bits += 6
            if (bits >= 8) {
                bits -= 8
                result[r++] = (buffer ushr bits and 0xFF).toByte()
            }
        }
        return result
    }

    /**
     * Safe comparison for CharArrays to prevent side-channel timing attacks.
     */
    fun contentEquals(a: CharArray?, b: CharArray?): Boolean {
        if (a === b) return true
        if (a == null || b == null) return false
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    /**
     * ELITE: Explicitly requests a system garbage collection to purge transient Strings from the heap.
     * Use this after high-stakes operations to signal the JVM to reclaim the heap space.
     */
    fun requestSystemGc() {
        try {
            System.gc()
        } catch (_: Exception) {}
    }
}
