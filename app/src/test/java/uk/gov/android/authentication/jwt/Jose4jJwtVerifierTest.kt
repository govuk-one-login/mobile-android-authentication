package uk.gov.android.authentication.jwt

import com.google.gson.JsonDeserializationContext
import org.json.JSONException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class Jose4jJwtVerifierTest {
    private val encodedJwt = ClassLoader.getSystemResource("./encodedJwt.txt").readText()

    private val jwk = "{\"crv\":\"P-256\",\"kid\":\"key-0\",\"kty\":\"EC\",\"x\":\"Shc8mJ6fcZik" +
        "hWM4ofHGSwXTkdqXM8GbPtRzPa7LttA=\",\"y\":\"OIhg/7rhWfmnWQEgAXzU8fCTggGrS3zj5x76a0l" +
        "rzJM=\"}"

    private val invalidJwk = "{\"crv\":\"P-256\",\"kid\":\"xuUZtjUzW2a-FgshCsLawbi08LL3aaHHhKKw" +
        "3w7O8x0\",\"kty\":\"EC\",\"x\":\"BY7gXGUvMrwrVuytSWVG4SAYD8dEYtUCdokR5q632xQ\",\"y" +
        "\":\"P3Zwqtz3XimgdwLEF-z7akHyiqAfsmfa5JfJlYHouZw\"}"

    private val sut = Jose4jJwtVerifier()

    @Test
    fun testWithValidJwtAndJwk() {
        val actualResult = sut.verify(encodedJwt, jwk)
        assertEquals(true, actualResult)
    }

    @Test
    fun testWithValidJwtAndInvalidJwk() {
        val actualResult = sut.verify(encodedJwt, invalidJwk)
        assertEquals(false, actualResult)
    }
}
