package uk.gov.android.authentication.login

import net.openid.appauth.ClientAuthentication

fun interface ClientAuthenticationProvider {
    fun setCustomClientAuthentication(
        clientAttestation: String?,
        proofOfPossession: String?
    ): ClientAuthentication
}

class ClientAuthenticationProviderImpl : ClientAuthenticationProvider {
    override fun setCustomClientAuthentication(
        clientAttestation: String?,
        proofOfPossession: String?
    ): ClientAuthentication {
        return object : ClientAuthentication {
            override fun getRequestHeaders(clientId: String): MutableMap<String, String> =
                mutableMapOf(
                    Pair(CLIENT_ATTESTATION, clientAttestation ?: ""),
                    Pair(PROOF_OF_POSSESSION, proofOfPossession ?: "")
                )

            override fun getRequestParameters(clientId: String): MutableMap<String, String> =
                mutableMapOf()
        }
    }

    companion object {
        private const val CLIENT_ATTESTATION = "OAuth-Client-Attestation"
        private const val PROOF_OF_POSSESSION = "OAuth-Client-Attestation-PoP"
    }
}
