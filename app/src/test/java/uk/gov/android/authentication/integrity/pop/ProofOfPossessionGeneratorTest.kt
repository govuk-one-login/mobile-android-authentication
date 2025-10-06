package uk.gov.android.authentication.integrity.pop

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import uk.gov.android.authentication.json.jwk.JWK

class ProofOfPossessionGeneratorTest {
    @Test
    fun `create Base64 Proof of Possession`() {
        val expectedResult = ClassLoader.getSystemResource("bodyPoPBase64.txt").readText()
        val result = ProofOfPossessionGenerator.createBase64PoP(
            "iss",
            "aud",
            0,
            "jti"
        )

        assertEquals(expectedResult, result)
    }

    @Test
    fun `test if PoP is expired - true`() {
        val expiredDateInSeconds =
            (Instant.now().toEpochMilli() - (MINUTES * MINUTE_IN_MILLISECONDS)) / CONVERT_TO_SECONDS

        assertTrue(ProofOfPossessionGenerator.isPopExpired(expiredDateInSeconds))
    }

    @Test
    fun `test if PoP is expired - false`() {
        val validDateInSeconds =
            (Instant.now().toEpochMilli() + (MINUTES * MINUTE_IN_MILLISECONDS)) / CONVERT_TO_SECONDS

        assertFalse(ProofOfPossessionGenerator.isPopExpired(validDateInSeconds))
    }

    @Test
    fun `create Base64 Refresh D Proof of Possession`() {
        val expectedResult = ClassLoader.getSystemResource("bodyDPoPBase64.txt").readText()
        val result = ProofOfPossessionGenerator.createBase64DPoP(
            JWK.generateSimpleJwk("x", "y"),
            "0",
            0
        )

        assertEquals(expectedResult, result)
    }

    companion object {
        private const val MINUTES = 3
        private const val MINUTE_IN_MILLISECONDS = 60000
        private const val CONVERT_TO_SECONDS = 1000
    }
}
