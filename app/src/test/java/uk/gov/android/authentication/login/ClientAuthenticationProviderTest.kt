package uk.gov.android.authentication.login

import kotlin.test.Test
import kotlin.test.assertEquals

class ClientAuthenticationProviderTest {
    private val sut = ClientAuthenticationProviderImpl()

    @Test
    fun `provide additional header parameters`() {
        // WHEN
        val result = sut.setCustomClientAuthentication(
            ATTESTATION,
            POP
        )

        // THEN
        assertEquals(expected, result.getRequestHeaders(""))
    }

    companion object {
        private const val ATTESTATION = "client attestation"
        private const val POP = "proof of possession"
        private val expected = mutableMapOf(
            Pair("OAuth-Client-Attestation", ATTESTATION),
            Pair("OAuth-Client-Attestation-PoP", POP)
        )
    }
}
