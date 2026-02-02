package uk.gov.android.authentication.integrity.did

import java.math.BigInteger
import java.util.stream.Stream
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class Base58Test {
    @ParameterizedTest
    @MethodSource("provideEncodeTestCases")
    fun `encode produces correct base58 string`(
        input: ByteArray,
        expected: String
    ) {
        assertEquals(expected, Base58.encode(input))
    }

    @ParameterizedTest
    @MethodSource("provideDecodeTestCases")
    fun `decode produces correct byte array`(
        input: String,
        expected: ByteArray
    ) {
        assertArrayEquals(expected, Base58.decode(input))
    }

    @ParameterizedTest
    @MethodSource("provideRoundTripTestCases")
    fun `encode and decode are inverse operations`(input: ByteArray) {
        val encoded = Base58.encode(input)
        val decoded = Base58.decode(encoded)
        assertArrayEquals(input, decoded)
    }

    @ParameterizedTest
    @ValueSource(strings = ["0", "O", "I", "l", "test0", "testO", "testI", "testl"])
    fun `decode throws exception for invalid characters`(input: String) {
        assertFailsWith<IllegalArgumentException> { Base58.decode(input) }
    }

    @Test
    fun `decode throws exception for non-ASCII character`() {
        assertFailsWith<IllegalArgumentException> { Base58.decode("test©") }
            .also { assertEquals("Invalid character '©' at position 4", it.message.toString()) }
    }

    @Test
    fun `decode throws exception with correct position for invalid character`() {
        assertFailsWith<IllegalArgumentException> { Base58.decode("abc0def") }
            .also { assertEquals("Invalid character '0' at position 3", it.message.toString()) }
    }

    @Test
    fun `alphabet excludes ambiguous characters`() {
        val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        assertEquals(58, alphabet.length)
        listOf('0', 'O', 'I', 'l').forEach { char ->
            assertEquals(false, alphabet.contains(char))
        }
    }

    @Test
    fun `encode with non-zero leading bytes`() {
        val result = Base58.encode(byteArrayOf(1, 2, 3))
        assertEquals("Ldp", result)
    }

    @Test
    fun `decode with non-encoded-zero leading characters`() {
        val result = Base58.decode("Ldp")
        assertArrayEquals(byteArrayOf(1, 2, 3), result)
    }

    @Test
    fun `encode with multiple leading zeros`() {
        val result = Base58.encode(byteArrayOf(0, 0, 0, 1))
        assertEquals("1112", result)
    }

    @Test
    fun `encode produces leading encoded zeros in output`() {
        val result = Base58.encode(byteArrayOf(0, 0, 0, 0, 1))
        assertEquals("11112", result)
    }

    @Test
    fun `encode with value that produces leading 1 in encoded output`() {
        val result = Base58.encode(
            byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        )
        assertTrue(result.startsWith("1"))
    }

    @Test
    fun `countLeadingEncodedZeros returns 0 for array without leading encoded zeros`() {
        val array = charArrayOf('2', '3', '4')
        assertEquals(0, with(Base58) { array.countLeadingEncodedZeros() })
    }

    @Test
    fun `countLeadingEncodedZeros returns count for array with leading encoded zeros`() {
        val array = charArrayOf('1', '1', '1', '2', '3')
        assertEquals(3, with(Base58) { array.countLeadingEncodedZeros() })
    }

    @Test
    fun `countLeadingEncodedZeros returns size for array with all encoded zeros`() {
        val array = charArrayOf('1', '1', '1', '1')
        assertEquals(array.size, with(Base58) { array.countLeadingEncodedZeros() })
    }

    companion object {
        @JvmStatic
        fun provideEncodeTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of(ByteArray(0), ""),
                Arguments.of(byteArrayOf(0), "1"),
                Arguments.of(ByteArray(7), "1111111"),
                Arguments.of(byteArrayOf(1), "2"),
                Arguments.of(byteArrayOf(57), "z"),
                Arguments.of(byteArrayOf(-1), "5Q"),
                Arguments.of(byteArrayOf(0, 0, 1, 2, 3), "11Ldp"),
                Arguments.of("Hello World".toByteArray(), "JxF12TrwUP45BMd"),
                Arguments.of("test".toByteArray(), "3yZe7d"),
                Arguments.of(BigInteger.valueOf(3471844090L).toByteArray(), "16Ho7Hs")
            )

        @JvmStatic
        fun provideDecodeTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of("", ByteArray(0)),
                Arguments.of("1", byteArrayOf(0)),
                Arguments.of("1111111", ByteArray(7)),
                Arguments.of("2", byteArrayOf(1)),
                Arguments.of("z", byteArrayOf(57)),
                Arguments.of("5Q", byteArrayOf(-1)),
                Arguments.of("11Ldp", byteArrayOf(0, 0, 1, 2, 3)),
                Arguments.of("JxF12TrwUP45BMd", "Hello World".toByteArray()),
                Arguments.of("3yZe7d", "test".toByteArray())
            )

        @JvmStatic
        fun provideRoundTripTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of(ByteArray(0)),
                Arguments.of(byteArrayOf(0)),
                Arguments.of(ByteArray(10) { 0 }),
                Arguments.of(byteArrayOf(1, 2, 3)),
                Arguments.of(byteArrayOf(0, 0, 1, 2, 3)),
                Arguments.of(ByteArray(10) { -1 }),
                Arguments.of(ByteArray(100) { it.toByte() }),
                Arguments.of("Hello World".toByteArray()),
                Arguments.of("The quick brown fox jumps over the lazy dog".toByteArray()),
                Arguments.of(byteArrayOf(0xAA.toByte(), 0x55, 0xAA.toByte(), 0x55)),
                Arguments.of(BigInteger.valueOf(3471844090L).toByteArray())
            )
    }
}
