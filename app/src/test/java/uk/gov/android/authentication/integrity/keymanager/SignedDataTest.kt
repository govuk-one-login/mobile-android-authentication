package uk.gov.android.authentication.integrity.keymanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SignedDataTest {
    @Test
    fun `equals returns true for same instance`() {
        val signedData = SignedData("alias", byteArrayOf(1, 2, 3))

        assertEquals(signedData, signedData)
    }

    @Test
    fun `equals returns true for equal content`() {
        val signedData1 = SignedData("alias", byteArrayOf(1, 2, 3))
        val signedData2 = SignedData("alias", byteArrayOf(1, 2, 3))

        assertEquals(signedData1, signedData2)
    }

    @Test
    fun `equals returns false for different alias`() {
        val signedData1 = SignedData("alias1", byteArrayOf(1, 2, 3))
        val signedData2 = SignedData("alias2", byteArrayOf(1, 2, 3))

        assertNotEquals(signedData1, signedData2)
    }

    @Test
    fun `equals returns false for different signature`() {
        val signedData1 = SignedData("alias", byteArrayOf(1, 2, 3))
        val signedData2 = SignedData("alias", byteArrayOf(4, 5, 6))

        assertNotEquals(signedData1, signedData2)
    }

    @Test
    fun `equals returns false for null`() {
        val signedData = SignedData("alias", byteArrayOf(1, 2, 3))

        assertNotEquals(signedData, null as Any?)
    }

    @Test
    fun `equals returns false for different type`() {
        val signedData = SignedData("alias", byteArrayOf(1, 2, 3))

        assertNotEquals(signedData, "string" as Any)
    }

    @Test
    fun `hashCode is consistent for equal objects`() {
        val signedData1 = SignedData("alias", byteArrayOf(1, 2, 3))
        val signedData2 = SignedData("alias", byteArrayOf(1, 2, 3))

        assertEquals(signedData1.hashCode(), signedData2.hashCode())
    }

    @Test
    fun `hashCode is different for different alias`() {
        val signedData1 = SignedData("alias1", byteArrayOf(1, 2, 3))
        val signedData2 = SignedData("alias2", byteArrayOf(1, 2, 3))

        assertNotEquals(signedData1.hashCode(), signedData2.hashCode())
    }

    @Test
    fun `hashCode is different for different signature`() {
        val signedData1 = SignedData("alias", byteArrayOf(1, 2, 3))
        val signedData2 = SignedData("alias", byteArrayOf(4, 5, 6))

        assertNotEquals(signedData1.hashCode(), signedData2.hashCode())
    }

    @Test
    fun `handles empty signature array`() {
        val signedData = SignedData("alias", byteArrayOf())

        assertEquals("alias", signedData.keyAlias)
        assertEquals(0, signedData.signature.size)
    }

    @Test
    fun `handles large signature array`() {
        val largeSignature = ByteArray(1024) { it.toByte() }
        val signedData = SignedData("alias", largeSignature)

        assertEquals("alias", signedData.keyAlias)
        assertEquals(1024, signedData.signature.size)
    }
}
