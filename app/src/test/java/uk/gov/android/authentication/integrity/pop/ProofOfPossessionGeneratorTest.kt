package uk.gov.android.authentication.integrity.pop

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfPossessionGeneratorTest {
    @Test
    fun `create Base64 Proof of Possession`() {
        val expectedResult = ClassLoader.getSystemResource("bodyPoPBase64.txt").readText()
        val result =
            ProofOfPossessionGenerator.createBase64PoP(
                "iss",
                "aud",
                0,
                "jti",
            )

        assertEquals(expectedResult, result)
    }
}
