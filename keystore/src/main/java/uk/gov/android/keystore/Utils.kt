package uk.gov.android.keystore

import java.math.BigInteger
import kotlin.io.encoding.Base64

object Utils {
    fun BigInteger.toFixedLengthBytes(length: Int): ByteArray {
        val valueBytes = this.toByteArray()
        return if (valueBytes.size > length) {
            valueBytes.copyOfRange(valueBytes.size - length, valueBytes.size)
        } else if (valueBytes.size < length) {
            val paddedBytes = ByteArray(length)
            System.arraycopy(valueBytes, 0, paddedBytes, length - valueBytes.size, valueBytes.size)
            paddedBytes
        } else {
            valueBytes
        }
    }

    /**
     * Encodes a byte array to Base64URL format without padding.
     *
     * Uses URL-safe Base64 encoding (RFC 4648) which replaces '+' with '-'
     * and '/' with '_', and omits padding characters ('=').
     *
     * @return Base64URL-encoded string without padding
     */
    fun ByteArray.toUrlSafeNoPaddingBase64(): String =
        Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(this)
}
