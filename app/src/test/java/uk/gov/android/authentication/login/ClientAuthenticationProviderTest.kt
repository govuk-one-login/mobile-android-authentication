package uk.gov.android.authentication.login

import kotlin.test.Test
import kotlin.test.assertEquals

class ClientAuthenticationProviderTest {
    private val sut = ClientAuthenticationProviderImpl()

    @Test
    fun `provide additional header parameters`() {
        val result = sut.setCustomClientAuthentication(
            ATTESTATION,
            POP,
            DPOP
        )

        assertEquals(expected, result.getRequestHeaders(""))
        assertEquals(mutableMapOf(), result.getRequestParameters(""))
    }

    @Test
    fun `provide additional header parameters when input null`() {
        val result = sut.setCustomClientAuthentication(null, null, null)

        assertEquals(expectedWhenNull, result.getRequestHeaders(""))
        assertEquals(mutableMapOf(), result.getRequestParameters(""))
    }

    companion object {
        private const val ATTESTATION = "client attestation"
        private const val POP = "proof of possession"
        private const val DPOP = "demonstrating proof of possession"
        private val expected = mutableMapOf(
            Pair("OAuth-Client-Attestation", ATTESTATION),
            Pair("OAuth-Client-Attestation-PoP", POP),
            Pair("DPoP", DPOP)
        )
        private val expectedWhenNull = mutableMapOf(
            Pair("OAuth-Client-Attestation", ""),
            Pair("OAuth-Client-Attestation-PoP", ""),
            Pair("DPoP", "")
        )
    }
}
