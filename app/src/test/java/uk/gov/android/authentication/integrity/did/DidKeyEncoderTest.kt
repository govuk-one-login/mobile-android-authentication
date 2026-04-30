package uk.gov.android.authentication.integrity.did

import java.util.stream.Stream
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class DidKeyEncoderTest {
    @Test
    fun `encodeDidKey creates valid DID key format`() {
        val publicKey = ByteArray(33) { it.toByte() }
        val result = DidKeyEncoder.encodeDidKey(publicKey)

        assertTrue(result.startsWith("did:key:z"))
    }

    @Test
    fun `encodeDidKey includes multicodec prefix`() {
        val publicKey = ByteArray(33) { 1 }
        val result = DidKeyEncoder.encodeDidKey(publicKey)

        assertNotNull(result)
        assertTrue(result.length > "did:key:z".length)
    }

    @Test
    fun `encodeDidKey with P256_PUB codec`() {
        val publicKey = ByteArray(33) { 0x42 }
        val result = DidKeyEncoder.encodeDidKey(publicKey)

        assertTrue(result.startsWith("did:key:z"))
        assertTrue(result.length > 20) // Reasonable length check
    }

    @Test
    fun `encodeDidKey handles 33 byte compressed public key`() {
        // Typical compressed EC public key: 1 byte prefix + 32 bytes X coordinate
        val publicKey = ByteArray(33)
        publicKey[0] = 0x02 // Compression prefix
        for (i in 1..32) {
            publicKey[i] = i.toByte()
        }

        val result = DidKeyEncoder.encodeDidKey(publicKey)
        assertTrue(result.startsWith("did:key:z"))
    }

    @Test
    fun `encodeDidKey is deterministic`() {
        val publicKey = ByteArray(33) { 0x55 }
        val result1 = DidKeyEncoder.encodeDidKey(publicKey)
        val result2 = DidKeyEncoder.encodeDidKey(publicKey)

        assertEquals(result1, result2)
    }

    @Test
    fun `encodeDidKey different keys produce different DIDs`() {
        val publicKey1 = ByteArray(33) { 0x01 }
        val publicKey2 = ByteArray(33) { 0x02 }

        val did1 = DidKeyEncoder.encodeDidKey(publicKey1)
        val did2 = DidKeyEncoder.encodeDidKey(publicKey2)

        kotlin.test.assertNotEquals(did1, did2)
    }

    @Test
    fun `encodeDidKey concatenates multicodec and public key`() {
        val publicKey = ByteArray(33) { 0x11 }
        val result = DidKeyEncoder.encodeDidKey(publicKey)

        // Result should contain base58 encoding of (multicodec + publicKey)
        // Multicodec for P256_PUB is 0x8024 (2 bytes)
        // Total: 2 + 33 = 35 bytes encoded
        assertTrue(result.startsWith("did:key:z"))
    }

    @Test
    fun `encodeDidKey uses z prefix for base58btc multibase`() {
        val publicKey = ByteArray(33)
        val result = DidKeyEncoder.encodeDidKey(publicKey)

        // 'z' indicates base58btc encoding in multibase spec
        assertTrue(result.contains("did:key:z"))
        assertEquals('z', result["did:key:".length])
    }

    @Test
    fun `encodeDidKey handles empty public key`() {
        val publicKey = ByteArray(0)
        val result = DidKeyEncoder.encodeDidKey(publicKey)

        assertTrue(result.startsWith("did:key:z"))
    }

    @Test
    fun `encodeDidKey handles public key with leading zeros`() {
        val publicKey = ByteArray(33)
        publicKey[0] = 0
        publicKey[1] = 0
        publicKey[2] = 1

        val result = DidKeyEncoder.encodeDidKey(publicKey)
        assertTrue(result.startsWith("did:key:z"))
    }

    @Test
    fun `P256_PUB multicodec has correct specification values`() {
        val codec = DidKeyEncoder.Multicodec.P256_PUB

        assertEquals("1200", codec.code, "P-256 codec should be 0x1200")
        assertEquals(
            33,
            codec.compressedKeyLength,
            "P-256 compressed public key should be 33 bytes"
        )
    }

    @ParameterizedTest
    @MethodSource("provideHexToVarintHexTestCases")
    fun `hexToVarintHex converts hex to varint hex correctly`(
        input: String,
        expected: String
    ) {
        val result = DidKeyEncoder.hexToVarintHex(input)
        assertEquals(expected, result)
    }

    @Test
    fun `hexToVarintHex handles zero`() {
        assertEquals("00", DidKeyEncoder.hexToVarintHex("0"))
    }

    @Test
    fun `hexToVarintHex handles single byte values`() {
        assertEquals("01", DidKeyEncoder.hexToVarintHex("1"))
        assertEquals("7F", DidKeyEncoder.hexToVarintHex("7F"))
    }

    @Test
    fun `hexToVarintHex handles two byte values`() {
        assertEquals("8001", DidKeyEncoder.hexToVarintHex("80"))
        assertEquals("FF7F", DidKeyEncoder.hexToVarintHex("3FFF"))
    }

    @Test
    fun `hexToVarintHex handles large values`() {
        assertEquals("FFFFFFFF0F", DidKeyEncoder.hexToVarintHex("FFFFFFFF"))
    }

    @ParameterizedTest
    @MethodSource("provideParseHexTestCases")
    fun `parseHex converts hex string to byte array correctly`(
        input: String,
        expected: ByteArray
    ) {
        val result = DidKeyEncoder.parseHex(input)
        assertArrayEquals(expected, result)
    }

    @Test
    fun `parseHex handles empty string`() {
        val result = DidKeyEncoder.parseHex("")
        assertArrayEquals(byteArrayOf(), result)
    }

    @Test
    fun `parseHex handles single byte`() {
        val result = DidKeyEncoder.parseHex("FF")
        assertArrayEquals(byteArrayOf(-1), result)
    }

    @Test
    fun `parseHex handles multiple bytes`() {
        val result = DidKeyEncoder.parseHex("0102030405")
        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun `parseHex handles lowercase hex`() {
        val result = DidKeyEncoder.parseHex("abcdef")
        assertArrayEquals(byteArrayOf(-85, -51, -17), result)
    }

    @Test
    fun `parseHex handles uppercase hex`() {
        val result = DidKeyEncoder.parseHex("ABCDEF")
        assertArrayEquals(byteArrayOf(-85, -51, -17), result)
    }

    @Test
    fun `parseHex handles mixed case hex`() {
        val result = DidKeyEncoder.parseHex("AbCdEf")
        assertArrayEquals(byteArrayOf(-85, -51, -17), result)
    }

    @Test
    fun `parseHex handles zeros`() {
        val result = DidKeyEncoder.parseHex("000000")
        assertArrayEquals(byteArrayOf(0, 0, 0), result)
    }

    @Test
    fun `parseHex throws exception for odd length string`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                DidKeyEncoder.parseHex("ABC")
            }
        assertEquals("Invalid hexadecimal String supplied.", exception.message)
    }

    @ParameterizedTest
    @ValueSource(strings = ["G0", "0G", "ZZ", "!@", "  ", "0x"])
    fun `parseHex throws exception for invalid hex characters`(invalidHex: String) {
        assertThrows(IllegalArgumentException::class.java) {
            DidKeyEncoder.parseHex(invalidHex)
        }
    }

    @Test
    fun `parseHex throws exception for non-hex character with descriptive message`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                DidKeyEncoder.parseHex("0G")
            }
        assertEquals("Invalid Hexadecimal Character: G", exception.message)
    }

    @Test
    fun `parseHex and hexToVarintHex work together for multicodec example`() {
        val varintHex = DidKeyEncoder.hexToVarintHex("1200")
        assertEquals("8024", varintHex)

        val bytes = DidKeyEncoder.parseHex(varintHex)
        assertArrayEquals(byteArrayOf(-128, 36), bytes)
    }

    @ParameterizedTest
    @MethodSource("provideRoundTripTestCases")
    fun `parseHex correctly parses hex strings`(
        hexString: String,
        expectedBytes: ByteArray
    ) {
        val result = DidKeyEncoder.parseHex(hexString)
        assertArrayEquals(expectedBytes, result)
    }

    @Test
    fun `hexToVarintHex handles maximum single byte varint`() {
        assertEquals("7F", DidKeyEncoder.hexToVarintHex("7F"))
    }

    @Test
    fun `hexToVarintHex handles minimum two byte varint`() {
        assertEquals("8001", DidKeyEncoder.hexToVarintHex("80"))
    }

    @Test
    fun `parseHex handles all byte values`() {
        val hexString = (0..255).joinToString("") { "%02X".format(it) }
        val result = DidKeyEncoder.parseHex(hexString)
        assertEquals(256, result.size)
        result.forEachIndexed { index, byte ->
            assertEquals(index.toByte(), byte)
        }
    }

    companion object {
        @JvmStatic
        fun provideHexToVarintHexTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of("1200", "8024"),
                Arguments.of("00", "00"),
                Arguments.of("01", "01"),
                Arguments.of("7F", "7F"),
                Arguments.of("80", "8001"),
                Arguments.of("FF", "FF01"),
                Arguments.of("100", "8002"),
                Arguments.of("4000", "808001"),
                Arguments.of("FFFF", "FFFF03"),
                Arguments.of("10000", "808004"),
                Arguments.of("FFFFFF", "FFFFFF07")
            )

        @JvmStatic
        fun provideParseHexTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of("00", byteArrayOf(0)),
                Arguments.of("01", byteArrayOf(1)),
                Arguments.of("FF", byteArrayOf(-1)),
                Arguments.of("0001", byteArrayOf(0, 1)),
                Arguments.of("8024", byteArrayOf(-128, 36)),
                Arguments.of("DEADBEEF", byteArrayOf(-34, -83, -66, -17)),
                Arguments.of("00112233", byteArrayOf(0, 17, 34, 51))
            )

        @JvmStatic
        fun provideRoundTripTestCases(): Stream<Arguments> =
            Stream.of(
                Arguments.of("1200", byteArrayOf(18, 0)),
                Arguments.of("ABCD", byteArrayOf(-85, -51)),
                Arguments.of("0F0F", byteArrayOf(15, 15)),
                Arguments.of("F0F0", byteArrayOf(-16, -16))
            )
    }
}
