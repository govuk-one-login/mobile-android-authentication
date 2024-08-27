package uk.gov.android.authentication

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenResponseTest {
    @Test
    fun `serialise TokenResponse to JSON all values`() {
        val tokenResponse = TokenResponse(
            "bearer",
            "sampleAccessToken",
            3600,
            "sampleIdToken",
            "sampleRefreshToken"
        )
        val expectedJson = "{\"tokenType\":\"bearer\",\"accessToken\":\"sampleAccessToken\"," +
            "\"accessTokenExpirationTime\":3600,\"idToken\":\"sampleIdToken\"," +
            "\"refreshToken\":\"sampleRefreshToken\"}"
        assertEquals(expectedJson, jsonSerialize(tokenResponse))
    }

    @Test
    fun `serialise TokenResponse to JSON with defaults`() {
        val tokenResponse = TokenResponse(
            "bearer",
            "sampleAccessToken",
            3600,
        )
        val expectedJson = "{\"tokenType\":\"bearer\",\"accessToken\":\"sampleAccessToken\"," +
            "\"accessTokenExpirationTime\":3600}"
        assertEquals(expectedJson, jsonSerialize(tokenResponse))
    }

    @Test
    fun `de-serialise TokenResponse from JSON`() {
        val json = "{\"tokenType\":\"bearer\",\"accessToken\":\"sampleAccessToken\"," +
            "\"accessTokenExpirationTime\":3600,\"idToken\":\"sampleIdToken\"," +
            "\"refreshToken\":\"sampleRefreshToken\"}"
        val expectedTokenResponse = TokenResponse(
            "bearer",
            "sampleAccessToken",
            3600,
            "sampleIdToken",
            "sampleRefreshToken"
        )
        assertEquals(expectedTokenResponse, jsonDeserialize(json))
    }

    @Test
    fun `de-serialise TokenResponse from JSON with defaults`() {
        val json = "{\"tokenType\":\"bearer\",\"accessToken\":\"sampleAccessToken\"," +
                "\"accessTokenExpirationTime\":3600}"
        val expectedTokenResponse = TokenResponse(
            "bearer",
            "sampleAccessToken",
            3600,
        )
        assertEquals(expectedTokenResponse, jsonDeserialize(json))
    }

    @Test
    fun testJsonDeserializationWithInvalidJson() {
        val invalidJson = "invalidJson"
        assertThrows<SerializationException> {
            jsonDeserialize(invalidJson)
        }
    }

    @Test
    fun testJsonDeserializationWithInvalidJsonType() {
        val invalidJson = "{\"test\":\"invalidJson\"}"
        assertThrows<IllegalArgumentException> {
            jsonDeserialize(invalidJson)
        }
    }

    private fun jsonSerialize(obj: TokenResponse): String {
        return Json.encodeToString(obj)
    }

    private fun jsonDeserialize(text: String): TokenResponse? {
        return Json.decodeFromString(text)
    }
}
