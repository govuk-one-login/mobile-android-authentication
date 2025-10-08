package uk.gov.android.authentication.login

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.OptIn
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.ExperimentalEphemeralBrowsing
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientAuthentication
import net.openid.appauth.TokenRequest
import uk.gov.android.authentication.integrity.AppIntegrityParameters
import uk.gov.android.authentication.login.refresh.DemonstratingProofOfPossessionManager
import uk.gov.android.authentication.login.refresh.SignedDPoP

class AppAuthSession : LoginSession {
    private var authService: AuthorizationService
    private var demonstratingProofOfPossessionManager: DemonstratingProofOfPossessionManager? = null
    private val clientAuthenticationProvider = ClientAuthenticationProviderImpl()

    @Deprecated(
        "This constructor method is now deprecated and replaced by a constructor" +
            " requesting a Demonstrating Proof Of Possession Manager to allow for DPoP" +
            " to be sent in the token request and enable use of refresh tokens" +
            " - will be removed on 7/12/25",
        ReplaceWith("uk.gov.android.authentication.login.AppAuthSession"),
        DeprecationLevel.WARNING
    )
    constructor(context: Context) {
        authService = AuthorizationService(context)
    }

    constructor(
        context: Context,
        demonstratingProofOfPossessionManager: DemonstratingProofOfPossessionManager
    ) {
        authService = AuthorizationService(context)
        this.demonstratingProofOfPossessionManager = demonstratingProofOfPossessionManager
    }

    internal fun initAuthService(service: AuthorizationService) {
        this.authService = service
    }

    @OptIn(ExperimentalEphemeralBrowsing::class)
    override fun present(
        launcher: ActivityResultLauncher<Intent>,
        configuration: LoginSessionConfiguration
    ) {
        val customEphemeralTabIntent = CustomTabsIntent.Builder()
            .setEphemeralBrowsingEnabled(true)
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .build()
        val intent = authService.getAuthorizationRequestIntent(
            configuration.createRequest(),
            customEphemeralTabIntent
        )
        launcher.launch(intent)
    }

    @Suppress("TooGenericExceptionCaught")
    override fun finalise(
        intent: Intent,
        appIntegrity: AppIntegrityParameters,
        httpServiceDomain: String,
        onSuccess: (tokens: TokenResponse) -> Unit,
        onFailure: (error: Throwable) -> Unit
    ) {
        try {
            val authResponse = AuthorizationResponse.fromIntent(intent)
            if (authResponse == null) {
                val exception = AuthorizationException.fromIntent(intent)

                onFailure(AuthenticationError.from(exception))
                return
            }

            // Create object that allows for additional headers/ body parameters
            demonstratingProofOfPossessionManager?.let {
                when (val signedDPoP = it.generateDPoP(httpServiceDomain)) {
                    is SignedDPoP.Success -> {
                        val clientAuthenticationWithExtraHeaders =
                            clientAuthenticationProvider.setCustomClientAuthentication(
                                appIntegrity.attestation,
                                appIntegrity.pop,
                                signedDPoP.popJwt
                            )

                        // Create the standard request
                        val request = authResponse.createTokenExchangeRequest()

                        performTokenRequest(
                            request = request,
                            clientAuthentication = clientAuthenticationWithExtraHeaders,
                            onSuccess = { tokens -> onSuccess(tokens) },
                            onFailure = { error -> onFailure(error) }
                        )
                    }
                    is SignedDPoP.Failure -> onFailure(
                        signedDPoP.error ?: DPoPManagerError(signedDPoP.reason)
                    )
                }
            } ?: onFailure(DPoPManagerError())
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    @Deprecated(
        "This method has been deprecated and replaces with finalise that accepts a htu" +
            " as a parameter to allow for the fetching and handling refresh tokens" +
            " - will be removed on 7/12/25",
        ReplaceWith("uk.gov.android.authentication.login.AppAuthSession#finalise"),
        DeprecationLevel.WARNING
    )
    @Suppress("TooGenericExceptionCaught")
    override fun finalise(
        intent: Intent,
        appIntegrity: AppIntegrityParameters,
        onSuccess: (tokens: TokenResponse) -> Unit,
        onFailure: (error: Throwable) -> Unit
    ) {
        try {
            val authResponse = AuthorizationResponse.fromIntent(intent)
            if (authResponse == null) {
                val exception = AuthorizationException.fromIntent(intent)

                onFailure(AuthenticationError.from(exception))
                return
            }

            // Create object that allows for additional headers/ body parameters
            val clientAuthenticationWithExtraHeaders =
                clientAuthenticationProvider.setCustomClientAuthentication(
                    appIntegrity.attestation,
                    appIntegrity.pop
                )

            // Create the standard request
            val request = authResponse.createTokenExchangeRequest()

            performTokenRequest(
                request = request,
                clientAuthentication = clientAuthenticationWithExtraHeaders,
                onSuccess = { tokens -> onSuccess(tokens) },
                onFailure = { error -> onFailure(error) }
            )
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    @Deprecated(
        message = "Please replace this with the alternative finalise function to use improved " +
            "error handling",
        replaceWith = ReplaceWith("uk.gov.android.authentication.login.AppAuthSession#finalise"),
        level = DeprecationLevel.WARNING
    )
    override fun finalise(
        intent: Intent,
        appIntegrity: AppIntegrityParameters,
        callback: (tokens: TokenResponse) -> Unit
    ) {
        val authResponse = AuthorizationResponse.fromIntent(intent)
        if (authResponse == null) {
            val exception = AuthorizationException.fromIntent(intent)

            throw AuthenticationError.from(exception)
        }

        // Create object that allows for additional headers/ body parameters
        val clientAuthenticationWithExtraHeaders =
            clientAuthenticationProvider.setCustomClientAuthentication(
                appIntegrity.attestation,
                appIntegrity.pop
            )

        // Create the standard request
        val request = authResponse.createTokenExchangeRequest()

        authService.performTokenRequest(
            request,
            clientAuthenticationWithExtraHeaders
        ) { response, exception ->
            callback(
                response?.toTokenResponse() ?: throw AuthenticationError.from(exception)
            )
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun performTokenRequest(
        request: TokenRequest,
        clientAuthentication: ClientAuthentication,
        onSuccess: (tokens: TokenResponse) -> Unit,
        onFailure: (error: Throwable) -> Unit
    ) {
        authService.performTokenRequest(
            request,
            clientAuthentication
        ) { response, exception ->
            try {
                val tokenResponse = response?.toTokenResponse()
                if (tokenResponse == null) {
                    onFailure(AuthenticationError.from(exception))
                } else {
                    onSuccess(tokenResponse)
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    companion object {
        private const val DPOP_MANAGER_INIT_ERROR = "Demonstrating Proof Of Possession Manager" +
            " has not been initialised! Please make sure you provide it in the" +
            " constructor AppAuthSession()."

        data class DPoPManagerError(val error: String = DPOP_MANAGER_INIT_ERROR) : Exception(error)
    }
}
