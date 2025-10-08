package uk.gov.android.authentication.login

import net.openid.appauth.ClientAuthentication

interface ClientAuthenticationProvider {
    @Deprecated(
        message = "Replace with setCustomClientAuthentication(...) that accepts a DPoP to " +
            "allow for refresh tokens to be obtained - will be removed on 7th of December",
        level = DeprecationLevel.WARNING
    )
    fun setCustomClientAuthentication(
        clientAttestation: String?,
        proofOfPossession: String?
    ): ClientAuthentication

    fun setCustomClientAuthentication(
        clientAttestation: String?,
        proofOfPossession: String?,
        dpop: String?
    ): ClientAuthentication
}

class ClientAuthenticationProviderImpl : ClientAuthenticationProvider {

    @Deprecated(
        "Replace with setCustomClientAuthentication(...) that accepts a DPoP to allow" +
            " for refresh tokens to be obtained - will be removed on 7th of December",
        ReplaceWith(
            "uk/gov/android/authentication/login/ClientAuthenticationProvider" +
                "#setCustomClientAuthentication"
        ),
        DeprecationLevel.WARNING
    )
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

    override fun setCustomClientAuthentication(
        clientAttestation: String?,
        proofOfPossession: String?,
        dpop: String?
    ): ClientAuthentication {
        return object : ClientAuthentication {
            override fun getRequestHeaders(clientId: String): MutableMap<String, String> =
                mutableMapOf(
                    Pair(CLIENT_ATTESTATION, clientAttestation ?: ""),
                    Pair(PROOF_OF_POSSESSION, proofOfPossession ?: ""),
                    Pair(DEMONSTRATING_PROOF_OF_POSSESSION, dpop ?: "")
                )

            override fun getRequestParameters(clientId: String): MutableMap<String, String> =
                mutableMapOf()
        }
    }

    companion object {
        private const val CLIENT_ATTESTATION = "OAuth-Client-Attestation"
        private const val PROOF_OF_POSSESSION = "OAuth-Client-Attestation-PoP"
        private const val DEMONSTRATING_PROOF_OF_POSSESSION = "DPoP"
    }
}
