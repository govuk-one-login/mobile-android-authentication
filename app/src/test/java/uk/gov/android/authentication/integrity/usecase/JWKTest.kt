package uk.gov.android.authentication.integrity.usecase

import kotlin.test.Test
import kotlin.test.assertEquals

class JWKTest {
    @Test
    fun `makeJWK sets defaults`() {
        val actual = JWK.makeJWK(X, Y)
        assertEquals(expected = "EC", actual = actual.keyType)
        assertEquals(expected = "sig", actual = actual.use)
    }

    companion object {
        const val X = "18wHLeIgW9wVN6VD1Txgpqy2LszYkMf6J8njVAibvhM"
        const val Y = "-V4dS4UaLMgP_4fY4j8ir7cl1TXlFdAgcx55o7TkcSA"
    }
}
