package uk.gov.android.authentication.integrity

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals

class AppIntegrityUtilsTest {
    @Test
    fun `convert array to fixed length bytes - input array too long`() {
        val input = BigInteger.valueOf(1001001011101)
        val arrByteArray = input.toByteArray()

        val expected = arrByteArray.copyOfRange(arrByteArray.size - 4, arrByteArray.size)
        val result = AppIntegrityUtils.toFixedLengthBytes(input, 4)

        assertEquals(expected.size, result.size)
    }

    @Test
    fun `convert array to fixed length bytes - input array too short`() {
        val input = BigInteger.valueOf(100)

        val expected = ByteArray(4)
        val result = AppIntegrityUtils.toFixedLengthBytes(input, 4)

        assertEquals(expected.size, result.size)
    }

    @Test
    fun `convert array to fixed length bytes - exact size`() {
        val input = BigInteger.valueOf(100)

        val expected = input.toByteArray()
        val result = AppIntegrityUtils.toFixedLengthBytes(input, 1)

        assertEquals(expected.size, result.size)
    }
}
