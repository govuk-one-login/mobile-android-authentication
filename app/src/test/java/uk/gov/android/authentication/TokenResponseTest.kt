package uk.gov.android.authentication

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TokenResponseTest {
    @Test
    fun testJsonSerialization() {
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
        assertEquals(expectedJson, tokenResponse.jsonSerializeString())
    }

    @Test
    fun testJsonDeserialization() {
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
        assertEquals(expectedTokenResponse, TokenResponse.jsonDeserialize(json))
    }

    @Test
    fun testJsonDeserializationWithInvalidJson() {
        val invalidJson = "invalidJson"
        assertNull(TokenResponse.jsonDeserialize(invalidJson))
    }

    @Test
    fun testJsonDeserializationWithInvalidJsonType() {
        val invalidJson = "{\"test\":\"invalidJson\"}"
        assertNull(TokenResponse.jsonDeserialize(invalidJson))
    }
}
