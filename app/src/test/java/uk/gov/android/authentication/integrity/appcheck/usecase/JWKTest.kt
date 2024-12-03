package uk.gov.android.authentication.integrity.appcheck.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class JWKTest {
    @Test
    fun `makeJWK sets defaults`() {
        val actual = JWK.makeJWK(X, Y)
        assertEquals(jwk, actual)
    }

    companion object {
        const val X = "18wHLeIgW9wVN6VD1Txgpqy2LszYkMf6J8njVAibvhM"
        const val Y = "-V4dS4UaLMgP_4fY4j8ir7cl1TXlFdAgcx55o7TkcSA"
        val jwk = JWK.JsonWebKey(
            jwk = JWK.JsonWebKeyFormat(
                "EC",
                "sig",
                "P-256",
                X,
                Y
            )
        )
    }
}
