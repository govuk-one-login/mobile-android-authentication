package uk.gov.android.authentication.login

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication

class AppAuthSession(
    context: Context,
) : LoginSession {
    private val authService: AuthorizationService = AuthorizationService(context)

    override fun present(
        launcher: ActivityResultLauncher<Intent>,
        configuration: LoginSessionConfiguration
    ) {
        val intent = authService.getAuthorizationRequestIntent(configuration.createRequest())
        launcher.launch(intent)
    }

    override fun finalise(
        intent: Intent,
        appIntegrity: Pair<String?, String?>,
        callback: (tokens: TokenResponse) -> Unit
    ) {
        val authResponse = AuthorizationResponse.fromIntent(intent)
        if (authResponse == null) {
            val exception = AuthorizationException.fromIntent(intent)

            throw AuthenticationError(
                exception?.message ?: "Auth response null",
                AuthenticationError.ErrorType.OAUTH
            )
        }

        val clientAuthenticationWithExtraHeaders = object : ClientAuthentication {
            override fun getRequestHeaders(clientId: String): MutableMap<String, String> =
                mutableMapOf(
                    Pair(CLIENT_ATTESTATION, appIntegrity.first ?: ""),
                    Pair(PROOF_OF_POSSESSION, appIntegrity.second ?: "")
                )

            override fun getRequestParameters(clientId: String): MutableMap<String, String> =
                mutableMapOf()
        }

        val request = authResponse.createTokenExchangeRequest()
        Log.d("TokenRequest", "${request.requestParameters}")
        authService.performTokenRequest(
            request,
            clientAuthenticationWithExtraHeaders
        ) { response, exception ->
            callback(
                response?.toTokenResponse() ?: throw AuthenticationError.from(exception)
            )
        }
    }

    companion object {
        private const val CLIENT_ATTESTATION = "OAuth-Client-Attestation"
        private const val PROOF_OF_POSSESSION = "OAuth-Client-Attestation-PoP"
    }
}
