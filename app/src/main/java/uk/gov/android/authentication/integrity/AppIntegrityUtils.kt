package uk.gov.android.authentication.integrity

import java.math.BigInteger

object AppIntegrityUtils {
    fun toFixedLengthBytes(
        value: BigInteger,
        length: Int,
    ): ByteArray {
        val valueBytes = value.toByteArray()
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
}
