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
}
