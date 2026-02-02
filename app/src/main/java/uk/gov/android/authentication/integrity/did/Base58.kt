/*
 * Copyright 2011 Google Inc.
 * Copyright 2018 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.android.authentication.integrity.did

import androidx.annotation.VisibleForTesting

internal object Base58 {
    private const val BASE_58 = 58u
    private const val BASE_256 = 256u
    private const val ASCII_TABLE_SIZE = 128

    private val ALPHABET =
        "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray()
    private val ENCODED_ZERO = ALPHABET[0]
    private val INDEXES =
        IntArray(ASCII_TABLE_SIZE) { -1 }.apply {
            ALPHABET.forEachIndexed { i, c -> this[c.code] = i }
        }

    private fun ByteArray.countLeadingZeros() = takeWhile { it == 0.toByte() }.size

    @VisibleForTesting
    fun CharArray.countLeadingEncodedZeros() = takeWhile { it == ENCODED_ZERO }.size

    /**
     * Decodes the given base58 string into the original data bytes.
     *
     * @param input the base58-encoded string to decode
     * @return the decoded data bytes
     * @throws IllegalArgumentException if the given string is not a valid base58 string
     */
    fun decode(input: String): ByteArray {
        if (input.isEmpty()) return ByteArray(0)

        // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
        val input58 = ByteArray(input.length)
        input.forEachIndexed { i, c ->
            val digit = if (c.code < ASCII_TABLE_SIZE) INDEXES[c.code] else -1
            require(digit >= 0) { "Invalid character '$c' at position $i" }
            input58[i] = digit.toByte()
        }

        val zeros = input58.countLeadingZeros()

        // Convert base-58 digits to base-256 digits.
        val decoded = ByteArray(input.length)
        var outputStart = decoded.size
        var inputStart = zeros
        while (inputStart < input58.size) {
            decoded[--outputStart] = divmod(input58, inputStart.toUInt(), BASE_58, BASE_256)
            if (input58[inputStart].toInt() == 0) ++inputStart
        }

        while (outputStart < decoded.size && decoded[outputStart].toInt() == 0) {
            ++outputStart
        }
        return decoded.copyOfRange(outputStart - zeros, decoded.size)
    }

    /**
     * Encodes the given bytes as a base58 string (no checksum is appended).
     *
     * @param input the bytes to encode
     * @return the base58-encoded string
     */
    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""

        var zeros = input.countLeadingZeros()

        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        val inputCopy = input.copyOf()
        val encoded = CharArray(input.size * 2)
        var outputStart = encoded.size
        var inputStart = zeros
        while (inputStart < inputCopy.size) {
            encoded[--outputStart] = ALPHABET[
                divmod(
                    inputCopy,
                    inputStart.toUInt(),
                    BASE_256,
                    BASE_58
                ).toInt()
            ]
            if (inputCopy[inputStart].toInt() == 0) ++inputStart
        }

        outputStart += encoded.copyOfRange(outputStart, encoded.size).countLeadingEncodedZeros()
        while (--zeros >= 0) encoded[--outputStart] = ENCODED_ZERO
        return String(encoded, outputStart, encoded.size - outputStart)
    }

    /**
     * Divides a number, represented as an array of bytes each containing a single digit
     * in the specified base, by the given divisor. The given number is modified in-place
     * to contain the quotient, and the return value is the remainder.
     *
     * @param number the number to divide
     * @param firstDigit the index within the array of the first non-zero digit
     *        (this is used for optimization by skipping the leading zeros)
     * @param base the base in which the number's digits are represented (up to 256)
     * @param divisor the number to divide by (up to 256)
     * @return the remainder of the division operation
     */
    private fun divmod(
        number: ByteArray,
        firstDigit: UInt,
        base: UInt,
        divisor: UInt
    ): Byte {
        var remainder = 0.toUInt()
        for (i in firstDigit.toInt() until number.size) {
            val digit = number[i].toUByte()
            val temp = remainder * base + digit
            number[i] = (temp / divisor).toByte()
            remainder = temp % divisor
        }
        return remainder.toByte()
    }
}
