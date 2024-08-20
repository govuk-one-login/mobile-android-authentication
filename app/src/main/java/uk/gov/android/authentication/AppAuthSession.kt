package uk.gov.android.authentication

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import java.util.UUID

@Suppress("TooGenericExceptionThrown")
class AppAuthSession(
    context: Context,
) : LoginSession {
    private val authService: AuthorizationService = AuthorizationService(context)

    override fun present(
        launcher: ActivityResultLauncher<Intent>,
        configuration: LoginSessionConfiguration
    ) {
        with(configuration) {
            val nonce = UUID.randomUUID().toString()

            val serviceConfig =
                AuthorizationServiceConfiguration(
                    authorizeEndpoint,
                    tokenEndpoint
                )

            val additionalParameters = mutableMapOf(
                "vtr" to vectorsOfTrust
            )
            persistentSessionId?.let {
                additionalParameters["govuk_signin_session_id"] = it
            }

            val builder =
                AuthorizationRequest.Builder(
                    serviceConfig,
                    clientId,
                    responseType.value,
                    redirectUri
                )
                    .setScopes(scopes.map { it.value })
                    .setUiLocales(locale.value)
                    .setNonce(nonce)
                    .setAdditionalParameters(additionalParameters)

            val authRequest = builder.build()

            val authIntent = authService.getAuthorizationRequestIntent(authRequest)
            launcher.launch(authIntent)
        }
    }

    override fun finalise(
        intent: Intent,
        callback: (tokens: TokenResponse) -> Unit
    ) {
        val authorizationResponse = AuthorizationResponse.fromIntent(intent)

        if (authorizationResponse == null) {
            val exception = AuthorizationException.fromIntent(intent)

            throw AuthenticationError(
                exception?.message ?: "Auth response was null",
                AuthenticationError.ErrorType.OAUTH
            )
        }

        val exchangeRequest = authorizationResponse.createTokenExchangeRequest()

        authService.performTokenRequest(
            exchangeRequest
        ) { response, exception ->
            if (response == null) {
                throw AuthenticationError(
                    exception?.message ?: "Failed token request",
                    AuthenticationError.ErrorType.OAUTH
                )
            }

            callback(response.toTokenResponse())
        }
    }
}
