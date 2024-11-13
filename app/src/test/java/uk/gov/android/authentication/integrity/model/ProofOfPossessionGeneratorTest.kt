package uk.gov.android.authentication.integrity.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ProofOfPossessionPackageTest {
    @Test
    fun `create Base64 Proof of Possession`() {
        val expectedResult = ClassLoader.getSystemResource("./bodyPoPBase64.txt").readText()
        val result = ProofOfPossessionPackage.createBase64PoP(
            "iss",
            "aud",
            0,
            "jti"
        )

        assertEquals(expectedResult, result)
    }
}
