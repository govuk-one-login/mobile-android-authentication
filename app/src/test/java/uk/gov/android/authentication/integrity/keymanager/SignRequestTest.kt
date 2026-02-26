package uk.gov.android.authentication.integrity.keymanager

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SignRequestTest {
    @Test
    fun `equals returns true for same instance`() {
        val request = SignRequest("alias", byteArrayOf(1, 2, 3))

        assertEquals(request, request)
    }

    @Test
    fun `equals returns true for equal content`() {
        val request1 = SignRequest("alias", byteArrayOf(1, 2, 3))
        val request2 = SignRequest("alias", byteArrayOf(1, 2, 3))

        assertEquals(request1, request2)
    }

    @Test
    fun `equals returns false for different alias`() {
        val request1 = SignRequest("alias1", byteArrayOf(1, 2, 3))
        val request2 = SignRequest("alias2", byteArrayOf(1, 2, 3))

        assertNotEquals(request1, request2)
    }

    @Test
    fun `equals returns false for different data`() {
        val request1 = SignRequest("alias", byteArrayOf(1, 2, 3))
        val request2 = SignRequest("alias", byteArrayOf(4, 5, 6))

        assertNotEquals(request1, request2)
    }

    @Test
    fun `equals returns false for null`() {
        val request = SignRequest("alias", byteArrayOf(1, 2, 3))

        assertNotEquals(request, null as Any?)
    }

    @Test
    fun `equals returns false for different type`() {
        val request = SignRequest("alias", byteArrayOf(1, 2, 3))

        assertNotEquals(request, "string" as Any)
    }

    @Test
    fun `hashCode is consistent for equal objects`() {
        val request1 = SignRequest("alias", byteArrayOf(1, 2, 3))
        val request2 = SignRequest("alias", byteArrayOf(1, 2, 3))

        assertEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `hashCode is different for different alias`() {
        val request1 = SignRequest("alias1", byteArrayOf(1, 2, 3))
        val request2 = SignRequest("alias2", byteArrayOf(1, 2, 3))

        assertNotEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `hashCode is different for different data`() {
        val request1 = SignRequest("alias", byteArrayOf(1, 2, 3))
        val request2 = SignRequest("alias", byteArrayOf(4, 5, 6))

        assertNotEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `handles empty data array`() {
        val request = SignRequest("alias", byteArrayOf())

        assertEquals("alias", request.keyAlias)
        assertEquals(0, request.data.size)
    }

    @Test
    fun `handles large data array`() {
        val largeData = ByteArray(1024) { it.toByte() }
        val request = SignRequest("alias", largeData)

        assertEquals("alias", request.keyAlias)
        assertEquals(1024, request.data.size)
    }
}
