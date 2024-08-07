package uk.gov.android.authentication

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import java.util.UUID
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration

@Suppress("TooGenericExceptionThrown")
class AppAuthSession(
    context: Context,
    private val authService: AuthorizationService = AuthorizationService(context)
) : LoginSession {

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
                    .setAdditionalParameters(
                        mapOf(
                            "vtr" to vectorsOfTrust,
                            "govuk_signin_session_id" to persistentSessionId
                        )
                    )

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

            callback(createFromAppAuthResponse(response))
        }
    }

    private fun createFromAppAuthResponse(
        response: net.openid.appauth.TokenResponse
    ): TokenResponse {
        return TokenResponse(
            tokenType = requireNotNull(response.tokenType) { "token type must not be empty" },
            accessToken =
            requireNotNull(response.accessToken) { "access token must not be empty" },
            accessTokenExpirationTime =
            requireNotNull(response.accessTokenExpirationTime) {
                "Token expiry must not be empty"
            },
            idToken = response.idToken,
            refreshToken = response.refreshToken
        )
    }

    companion object {
        const val REQUEST_CODE_AUTH = 418
    }
}
