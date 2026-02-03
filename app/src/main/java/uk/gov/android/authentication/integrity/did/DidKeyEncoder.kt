package uk.gov.android.authentication.integrity.did

import androidx.annotation.VisibleForTesting
import java.math.BigInteger

object DidKeyEncoder {
    private const val HEX_RADIX = 16
    private const val VARINT_SHIFT_BITS = 7
    private const val VARINT_CONTINUATION_BIT = 0x80
    private const val HEX_NIBBLE_BITS = 4
    private const val HEX_CHAR_LENGTH = 2
    private const val P256_COMPRESSED_KEY_LENGTH = 33

    /**
     * Creates the did key by concatenating the Multicodec value with the public key and encoding
     * it in base 58 format
     *
     * @param publicKey raw public key bytes
     * @return Formatted did key
     */
    fun encodeDidKey(publicKey: ByteArray): String {
        // get the Multicodec (that is a prefix with the unsigned varint of the algorithm type)
        val unsignedVarintCode = hexToVarintHex(Multicodec.P256_PUB.code)
        val multicodec = parseHex(unsignedVarintCode)
        // create a new byte array that "fits" the Multicodec + compressed public key
        val buffer = ByteArray(multicodec.size + publicKey.size)

        // concatenate the Multicodec and the compressed public key
        System.arraycopy(multicodec, 0, buffer, 0, multicodec.size)
        System.arraycopy(publicKey, 0, buffer, multicodec.size, publicKey.size)

        // base58 encode the buffer
        val base58Encoded = Base58.encode(buffer)

        // prefix with `z` to indicate multi-base base58btc encoding
        return "did:key:z$base58Encoded"
    }

    /**
     * An unsigned varint is typically used in over-the-wire transmission of integers. The use less
     * storage than a standard binary representation for small values, and more storage for larger
     * values. This is used as the vast majority of integer values used in computer systems are
     * small.
     *
     * <ol>
     *   <li>Convert the integer value to binary representation.
     *   <li>Group the binary digits into groups of 7 bits, with the most significant bit (MSB) of
     *       each group indicating whether more bytes follow.
     *   <li>Write the groups of 7 bits, with the MSB set to 1 for all groups except the last one.
     *   <li>If necessary, pad the last group with zero bits to make it a full 7 bits.
     *   <li>Convert each group to its hexadecimal representation.
     *   <li>Concatenate the hexadecimal representations to form the varint.
     * </ol>
     *
     * As an example:
     *
     * <ol>
     *   <li>Convert 0x1200 to binary: 1001000000000
     *   <li>Group into 7-bit groups: 0100100, 0000000
     *   <li>Add MSB for each group: 10000000, 00100100
     *   <li>Convert to hexadecimal: 0x80, 0x24
     *   <li>Concatenate: 0x8024
     * </ol>
     *
     * @param hex The hex value to convert
     * @return The varint value of the hex
     */
    @VisibleForTesting
    fun hexToVarintHex(hex: String): String {
        var bigInteger = BigInteger(hex, HEX_RADIX)
        val varintBytes = mutableListOf<Byte>()

        while (true) {
            val b = bigInteger.toByte()
            bigInteger = bigInteger.shiftRight(VARINT_SHIFT_BITS)
            if (bigInteger == BigInteger.ZERO) {
                varintBytes.add(b)
                break
            } else {
                varintBytes.add((b.toInt() or VARINT_CONTINUATION_BIT).toByte())
            }
        }

        return varintBytes.joinToString("") { "%02X".format(it) }
    }

    @VisibleForTesting
    fun parseHex(hexString: String): ByteArray {
        require(hexString.length % HEX_CHAR_LENGTH == 0) { "Invalid hexadecimal String supplied." }

        return ByteArray(hexString.length / HEX_CHAR_LENGTH) { i ->
            hexToByte(
                hexString.substring(i * HEX_CHAR_LENGTH, i * HEX_CHAR_LENGTH + HEX_CHAR_LENGTH)
            )
        }
    }

    private fun hexToByte(hexString: String): Byte {
        val firstDigit = toDigit(hexString[0])
        val secondDigit = toDigit(hexString[1])
        return ((firstDigit shl HEX_NIBBLE_BITS) + secondDigit).toByte()
    }

    private fun toDigit(hexChar: Char): Int =
        requireNotNull(hexChar.digitToIntOrNull(HEX_RADIX)) {
            "Invalid Hexadecimal Character: $hexChar"
        }

    /**
     * Multicodec identifier for cryptographic key types.
     *
     * @property code The codec code value in hexadecimal
     * @property compressedKeyLength The compressed public key length in bytes
     */
    enum class Multicodec(
        val code: String,
        val compressedKeyLength: Int
    ) {
        /** P-256 (secp256r1) public key codec */
        P256_PUB("1200", P256_COMPRESSED_KEY_LENGTH)
    }
}
