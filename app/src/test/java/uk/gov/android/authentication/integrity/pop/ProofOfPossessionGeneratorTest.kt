package uk.gov.android.authentication.integrity.pop

import java.time.Instant
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator.createBase64DPoP
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator.createBase64DidKeyPoP
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator.createBase64PoP
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator.getExpiryTime
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator.getIssueTime
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator.isPopExpired
import uk.gov.android.authentication.json.jwk.JWK

class ProofOfPossessionGeneratorTest {
    // createBase64PoP tests
    @Test
    fun `createBase64PoP generates expected JWT structure`() {
        val expectedResult = ClassLoader.getSystemResource("bodyPoPBase64.txt").readText()
        val result = createBase64PoP("iss", "aud", 0, "jti")

        assertEquals(expectedResult, result)
    }

    @Test
    fun `createBase64PoP contains correct header and payload fields`() {
        val result = createBase64PoP("test-iss", "test-aud", 9999L, "test-jti")

        val parts = result.split(".")
        assertEquals(2, parts.size)

        val header = decodeJsonObject(parts[0])
        assertEquals("ES256", header["alg"]?.jsonPrimitive?.content)
        assertEquals("oauth-client-attestation-pop+jwt", header["typ"]?.jsonPrimitive?.content)

        val payload = decodeJsonObject(parts[1])
        assertEquals("test-iss", payload["iss"]?.jsonPrimitive?.content)
        assertEquals("test-aud", payload["aud"]?.jsonPrimitive?.content)
        assertEquals(9999L, payload["exp"]?.jsonPrimitive?.content?.toLong())
        assertEquals("test-jti", payload["jti"]?.jsonPrimitive?.content)
    }

    @Test
    fun `createBase64PoP generates random jti when not provided`() {
        val result1 = createBase64PoP("iss", "aud", 1234567890L)
        val result2 = createBase64PoP("iss", "aud", 1234567890L)

        assertNotEquals(result1, result2)
    }

    @Test
    fun `createBase64PoP handles special characters and edge cases`() {
        val result = createBase64PoP("iss@#$%", "aud!@#", 0L, "jti")

        val payload = decodeJsonObject(result.split(".")[1])
        assertEquals("iss@#$%", payload["iss"]?.jsonPrimitive?.content)
        assertEquals("aud!@#", payload["aud"]?.jsonPrimitive?.content)
        assertEquals(0L, payload["exp"]?.jsonPrimitive?.content?.toLong())
    }

    // createBase64DPoP tests
    @Test
    fun `createBase64DPoP generates expected JWT structure`() {
        val expectedResult = ClassLoader.getSystemResource("bodyDPoPBase64.txt").readText()
        val result = createBase64DPoP(
            JWK.generateJwk("x", "y"),
            "test",
            "0",
            0
        )

        assertEquals(expectedResult, result)
    }

    @Test
    fun `createBase64DPoP contains correct header and payload fields`() {
        val jwk = JWK.generateJwk("x", "y")
        val result = createBase64DPoP(jwk, "https://token.example.com", "test-jti", 1234567890L)

        val parts = result.split(".")
        assertEquals(2, parts.size)

        val header = decodeJsonObject(parts[0])
        assertEquals("ES256", header["alg"]?.jsonPrimitive?.content)
        assertEquals("dpop+jwt", header["typ"]?.jsonPrimitive?.content)
        assertTrue(header.containsKey("jwk"))

        val payload = decodeJsonObject(parts[1])
        assertEquals("test-jti", payload["jti"]?.jsonPrimitive?.content)
        assertEquals("POST", payload["htm"]?.jsonPrimitive?.content)
        assertEquals("https://token.example.com", payload["htu"]?.jsonPrimitive?.content)
        assertEquals(1234567890L, payload["iat"]?.jsonPrimitive?.content?.toLong())
    }

    @Test
    fun `createBase64DPoP generates random jti and uses current time by default`() {
        val jwk = JWK.generateJwk("x", "y")
        val beforeTime = Instant.now().toEpochMilli() / CONVERT_TO_SECONDS

        val result1 = createBase64DPoP(jwk, "https://token.example.com")
        val result2 = createBase64DPoP(jwk, "https://token.example.com")

        val afterTime = Instant.now().toEpochMilli() / CONVERT_TO_SECONDS

        assertNotEquals(result1, result2)

        val payload = decodeJsonObject(result1.split(".")[1])
        val iat = payload["iat"]?.jsonPrimitive?.content?.toLong() ?: 0
        assertTrue(iat in beforeTime..afterTime)
    }

    @Test
    fun `createBase64DPoP handles different URL formats`() {
        val jwk = JWK.generateJwk("x", "y")
        val urls = listOf(
            "https://example.com",
            "https://example.com/path",
            "https://example.com/path?query=value",
            "https://example.com:8080/path"
        )

        urls.forEach { url ->
            val result = createBase64DPoP(jwk, url, "jti", 1234567890L)
            val payload = decodeJsonObject(result.split(".")[1])
            assertEquals(url, payload["htu"]?.jsonPrimitive?.content)
        }
    }

    // createBase64DidKeyPoP tests
    @Test
    fun `createBase64DidKeyPoP contains correct header and payload fields`() {
        val result = createBase64DidKeyPoP(
            kid = "did:key:z6MkTestKeyId123",
            nonce = "unique-nonce-12345",
            aud = "https://issuer.example.com",
            iss = ISS
        )

        val parts = result.split(".")
        assertEquals(2, parts.size)

        val header = decodeJsonObject(parts[0])
        assertEquals("ES256", header["alg"]?.jsonPrimitive?.content)
        assertEquals("openid4vci-proof+jwt", header["typ"]?.jsonPrimitive?.content)
        assertEquals("did:key:z6MkTestKeyId123", header["kid"]?.jsonPrimitive?.content)

        val payload = decodeJsonObject(parts[1])
        assertEquals(ISS, payload["iss"]?.jsonPrimitive?.content)
        assertEquals("unique-nonce-12345", payload["nonce"]?.jsonPrimitive?.content)
        assertEquals("https://issuer.example.com", payload["aud"]?.jsonPrimitive?.content)
        assertTrue(payload.containsKey("iat"))
    }

    @Test
    fun `createBase64DidKeyPoP generates current timestamp for iat`() {
        val beforeTime = System.currentTimeMillis() / CONVERT_TO_SECONDS

        val result = createBase64DidKeyPoP(
            kid = "did:key:z6MkTest",
            nonce = "test-nonce",
            aud = "https://example.com",
            iss = ISS
        )

        val afterTime = System.currentTimeMillis() / CONVERT_TO_SECONDS
        val payload = decodeJsonObject(result.split(".")[1])
        val iat = payload["iat"]?.jsonPrimitive?.content?.toLong() ?: 0

        assertTrue(iat in beforeTime..afterTime)
    }

    @Test
    fun `createBase64DidKeyPoP handles special characters and edge cases`() {
        val testCases = listOf(
            Triple(
                "did:key:z6Mk!@#$%^&*()",
                "nonce-with-special-chars-!@#$%",
                "https://example.com/path?param=value&other=test"
            ),
            Triple("did:key:z6MkTest", "", "https://example.com")
        )

        testCases.forEach { (kid, nonce, aud) ->
            val result = createBase64DidKeyPoP(kid, nonce, aud, ISS)

            val header = decodeJsonObject(result.split(".")[0])
            assertEquals(kid, header["kid"]?.jsonPrimitive?.content)

            val payload = decodeJsonObject(result.split(".")[1])
            assertEquals(nonce, payload["nonce"]?.jsonPrimitive?.content)
            assertEquals(aud, payload["aud"]?.jsonPrimitive?.content)
        }
    }

    // Common JWT encoding tests
    @Test
    fun `all factory methods use URL-safe base64 encoding without padding`() {
        val jwk = JWK.generateJwk("x", "y")
        val results = listOf(
            createBase64PoP("iss", "aud", 1234567890L, "jti"),
            createBase64DPoP(jwk, "https://example.com", "jti", 1234567890L),
            createBase64DidKeyPoP("did:key:z6MkTest", "nonce", "https://example.com", ISS)
        )

        results.forEach { result ->
            assertFalse(result.contains("+"), "Should not contain + character")
            assertFalse(result.contains("/"), "Should not contain / character")
            assertFalse(result.contains("="), "Should not contain padding")
            assertEquals(2, result.split(".").size, "Should have exactly 2 parts")
        }
    }

    // Utility method tests
    @Test
    fun `getExpiryTime returns timestamp 3 minutes in future`() {
        val currentTime = Instant.now().toEpochMilli() / CONVERT_TO_SECONDS
        val expiryTime = getExpiryTime()
        val difference = expiryTime - currentTime

        assertTrue(expiryTime > currentTime)
        assertTrue(difference in 179..181)
    }

    @Test
    fun `getIssueTime returns current timestamp`() {
        val beforeTime = Instant.now().toEpochMilli() / CONVERT_TO_SECONDS
        val issueTime = getIssueTime()
        val afterTime = Instant.now().toEpochMilli() / CONVERT_TO_SECONDS

        assertTrue(issueTime in beforeTime..afterTime)
    }

    @Test
    fun `isPopExpired correctly validates expiration`() {
        val pastTime = (Instant.now().toEpochMilli() - 10000) / CONVERT_TO_SECONDS
        val currentTime = Instant.now().toEpochMilli() / CONVERT_TO_SECONDS
        val futureTime = (Instant.now().toEpochMilli() + 10000) / CONVERT_TO_SECONDS

        assertTrue(isPopExpired(pastTime))
        assertTrue(isPopExpired(currentTime))
        assertFalse(isPopExpired(futureTime))
    }

    @Test
    fun `getUrlSafeNoPaddingBase64 encodes correctly`() {
        val input = "test data".toByteArray()
        val result1 = getUrlSafeNoPaddingBase64(input)
        val result2 = getUrlSafeNoPaddingBase64(input)

        assertEquals(result1, result2)
        assertFalse(result1.contains("+"))
        assertFalse(result1.contains("/"))
        assertFalse(result1.contains("="))

        val emptyResult = getUrlSafeNoPaddingBase64(ByteArray(0))
        assertEquals("", emptyResult)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun decodeJsonObject(base64: String): kotlinx.serialization.json.JsonObject {
        val decoded = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).decode(base64)
        return Json.parseToJsonElement(decoded.decodeToString()).jsonObject
    }

    companion object {
        private const val CONVERT_TO_SECONDS = 1000
        private const val ISS = "urn:fdc:gov:uk:wallet"
    }
}
