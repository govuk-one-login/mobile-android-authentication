package uk.gov.android.authentication.login

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenRequest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.android.authentication.integrity.AppIntegrityParameters
import uk.gov.android.authentication.login.AuthenticationError.Companion.NULL_AUTH_MESSAGE
import uk.gov.android.authentication.login.AuthenticationError.ErrorType
import uk.gov.android.authentication.login.refresh.DemonstratingProofOfPossessionManager
import uk.gov.android.authentication.login.refresh.SignedDPoP

class AppAuthSessionTest {
    private lateinit var appAuthSession: AppAuthSession
    private lateinit var demonstratingProofOfPossessionManager:
        DemonstratingProofOfPossessionManager
    private var authResponse = "{\n" +
        " \"request\": {\n" +
        "    \"configuration\": {\n" +
        "      \"authorizationEndpoint\": \"https://<your-authorization-server>/authorize\",\n" +
        "      \"tokenEndpoint\": \"https://<your-authorization-server>/token\"\n" +
        "    },\n" +
        "    \"responseType\": \"code\",\n" +
        "    \"clientId\": \"your_client_id\",\n" +
        "    \"redirectUri\": \"https://<your-authorization-server>/redirect\",\n" +
        "    \"scopes\": [\n" +
        "      \"openid\",\n" +
        "      \"profile\",\n" +
        "      \"email\"\n" +
        "    ],\n" +
        "    \"state\": \"your_state\",\n" +
        "    \"codeVerifier\": \"codeV_f1ctive_openid_test_987xyz_123_thgs45-swhsjdn\",\n" +
        "    \"additionalParameters\": {}\n" +
        "  },\n" +
        "  \"state\": \"your_state\",\n" +
        "  \"code\": \"auth_f1ct1ve_openid_c0de_xyz987\",\n" +
        "  \"codeVerifier\": \"codeV_f1ctive_openid_test_987xyz_123_thgs45-swhsjdn\",\n" +
        "  \"additionalParameters\": {}\n" +
        "}"
    private val authService: AuthorizationService = mock()

    @BeforeTest
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        demonstratingProofOfPossessionManager = mock()
        appAuthSession = AppAuthSession(context, demonstratingProofOfPossessionManager)
        appAuthSession.initAuthService(authService)
    }

    @Test
    fun presentCreatesAndLaunchesAuthorisationIntent() {
        // Given a mock ActivityResultLauncher
        val launcher: ActivityResultLauncher<Intent> = mock()
        whenever(launcher.launch(any())).then { } // take no action
        val loginSessionConfig = LoginSessionConfigurationTest.defaultConfig.copy()
        appAuthSession.initAuthService(authService)
        whenever(authService.getAuthorizationRequestIntent(any(), any())).thenReturn(Intent())

        // When calling present
        appAuthSession.present(
            launcher = launcher,
            configuration = loginSessionConfig
        )
        // Then create the AuthorisationIntent and launch
        verify(launcher).launch(any())
    }

    @Test
    fun finaliseThrowsIllegalArgumentExceptionForMalformedIntentResponse() {
        // Given an intent with a malformed (empty) response data JSON extra
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, "{}")
        }
        var t: Throwable? = null
        // When calling finalise
        appAuthSession.finalise(
            intent,
            AppIntegrityParameters(ATTESTATION, POP),
            "domain",
            {},
            { error -> t = error }
        )

        assertThat("Thrown error is not IllegalArgumentException", t is IllegalArgumentException)
    }

    @Test
    fun finaliseWithDPoPThrowsAuthenticationErrorOfAccessDenied() {
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.ACCESS_DENIED.code,
            AuthorizationRequestErrors.ACCESS_DENIED.error,
            AuthorizationRequestErrors.ACCESS_DENIED.errorDescription,
            AuthorizationRequestErrors.ACCESS_DENIED.errorUri,
            AuthorizationRequestErrors.ACCESS_DENIED.cause
        )
        // Given an intent with a malformed (empty) response data JSON extra
        val intent = Intent().putExtra(
            AuthorizationException.EXTRA_EXCEPTION,
            exception.toJsonString()
        )
        // When calling finaliseWithDPoP
        var t: Throwable? = null

        appAuthSession.finalise(
            intent,
            AppIntegrityParameters(ATTESTATION, POP),
            "domain",
            {},
            { error -> t = error }
        )

        // Then throw an IllegalArgumentException
        assertThat("Error is not of type AuthenticationError", t is AuthenticationError)
        val error = (t as AuthenticationError)
        assertEquals(AuthenticationError.ErrorType.ACCESS_DENIED, error.type)
    }

    @Test
    fun finaliseWithDPoPThrowsAuthenticationErrorForIntentWithoutResponse() {
        // Given an (empty) intent without a response data JSON extra
        val intent = Intent()
        // When calling finaliseWithDPoP
        var t: Throwable? = null

        appAuthSession.finalise(
            intent,
            AppIntegrityParameters(ATTESTATION, POP),
            "domain",
            {},
            { error -> t = error }
        )

        // Then throw an AuthenticationError
        assertThat("Error is not of type AuthenticationError", t is AuthenticationError)
        val error = (t as AuthenticationError)
        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
        assertEquals(AuthenticationError.Companion.NULL_AUTH_MESSAGE, error.message)
    }

    @Test
    fun finaliseWithDPoPThrowsAuthenticationErrorForIntentWithValidResponse() {
        // Given an intent with a response data JSON extra
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, authResponse)
        }
        // When calling finaliseWithDPoP
        appAuthSession.finalise(
            intent,
            AppIntegrityParameters(ATTESTATION, POP),
            "domain",
            {},
            {}
        )
    }

    @Test
    fun finaliseWithDPoPOnSuccessCalled() {
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, authResponse)
        }
        // When calling finaliseWithDPoP
        var actualTokenResponse: TokenResponse? = null

        val auTokenResponse = buildTokenResponse(accessToken = ACCESS_TOKEN)
        val expectedTokenResponse = TokenResponse(
            TOKEN_TYPE,
            ACCESS_TOKEN,
            EXPIRATION_TIME,
            ID_TOKEN,
            REFRESH_TOKEN
        )

        whenever(demonstratingProofOfPossessionManager.generateDPoP(any())).thenReturn(
            SignedDPoP.Success("success")
        )
        whenever(authService.performTokenRequest(any(), any(), any()))
            .thenAnswer {
                (it.arguments[2] as AuthorizationService.TokenResponseCallback)
                    .onTokenRequestCompleted(auTokenResponse, null)
            }

        appAuthSession.finalise(
            intent,
            AppIntegrityParameters(ATTESTATION, POP),
            "domain",
            { tr -> actualTokenResponse = tr },
            {}
        )

        assertEquals(expectedTokenResponse, actualTokenResponse)
    }

    @Test
    fun finaliseWithDPoPOnFailure() {
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, authResponse)
        }

        var actualException: Throwable? = null
        val auException = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            1005,
            "server_error",
            null,
            null,
            null
        )
        val expectedException = AuthenticationError(
            message = NULL_AUTH_MESSAGE,
            type = ErrorType.SERVER_ERROR,
            status = 1005
        )

        whenever(demonstratingProofOfPossessionManager.generateDPoP(any())).thenReturn(
            SignedDPoP.Success("success")
        )
        whenever(authService.performTokenRequest(any(), any(), any()))
            .thenAnswer {
                (it.arguments[2] as AuthorizationService.TokenResponseCallback)
                    .onTokenRequestCompleted(null, auException)
            }

        appAuthSession.finalise(
            intent,
            AppIntegrityParameters(ATTESTATION, POP),
            "domain",
            {},
            { error -> actualException = error }
        )

        assertEquals(expectedException, actualException)
    }

    @Test
    fun finaliseWithDPoPFailedSignDPoP() {
        val exp = Exception("Failure signing DPoP")
        var actualException: Throwable? = null

        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, authResponse)
        }

        whenever(demonstratingProofOfPossessionManager.generateDPoP(any()))
            .thenReturn(SignedDPoP.Failure(exp.message!!, exp))

        appAuthSession.finalise(
            intent,
            AppIntegrityParameters(ATTESTATION, POP),
            "domain",
            {},
            { error -> actualException = error }
        )

        assertEquals(exp, actualException)
    }

    @Test
    fun finaliseWithDPoPOnFailureToken() {
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, authResponse)
        }
        // When calling finaliseWithDPoP
        var actualException: Throwable? = null

        val auTokenResponse = buildTokenResponse(accessToken = null)

        whenever(demonstratingProofOfPossessionManager.generateDPoP(any())).thenReturn(
            SignedDPoP.Success("success")
        )
        whenever(authService.performTokenRequest(any(), any(), any()))
            .thenAnswer {
                (it.arguments[2] as AuthorizationService.TokenResponseCallback)
                    .onTokenRequestCompleted(auTokenResponse, null)
            }

        appAuthSession.finalise(
            intent,
            AppIntegrityParameters(ATTESTATION, POP),
            "domain",
            {},
            { error -> actualException = error }
        )

        assertThat(
            "Exception thrown is not IllegalArgumentException",
            actualException is IllegalArgumentException
        )
    }

    @Test
    fun finaliseWithDPoPFailureWhenNoErrorPassedFromSignedDPoPResult() {
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, authResponse)
        }
        // When calling finaliseWithDPoP
        var actualException: Throwable? = null
        whenever(demonstratingProofOfPossessionManager.generateDPoP(any())).thenReturn(
            SignedDPoP.Failure("Failure")
        )
        appAuthSession.finalise(
            intent,
            AppIntegrityParameters(ATTESTATION, POP),
            "domain",
            {},
            { error -> actualException = error }
        )

        assertEquals(AppAuthSession.Companion.DPoPManagerError("Failure"), actualException)
    }

    @Test(expected = IllegalArgumentException::class)
    fun deprecatedFinaliseThrowsIllegalArgumentExceptionForMalformedIntentResponse() {
        // Given an intent with a malformed (empty) response data JSON extra
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, "{}")
        }
        // When calling finalise
        appAuthSession.finalise(intent, AppIntegrityParameters(ATTESTATION, POP)) {}
        // Then throw an IllegalArgumentException
    }

    @Test
    fun deprecatedFinaliseThrowsAuthenticationErrorOfAccessDenied() {
        val exception = AuthorizationException(
            AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
            AuthorizationRequestErrors.ACCESS_DENIED.code,
            AuthorizationRequestErrors.ACCESS_DENIED.error,
            AuthorizationRequestErrors.ACCESS_DENIED.errorDescription,
            AuthorizationRequestErrors.ACCESS_DENIED.errorUri,
            AuthorizationRequestErrors.ACCESS_DENIED.cause
        )
        // Given an intent with a malformed (empty) response data JSON extra
        val intent = Intent().putExtra(
            AuthorizationException.EXTRA_EXCEPTION,
            exception.toJsonString()
        )
        // When calling finalise
        val result = assertThrows(AuthenticationError::class.java) {
            appAuthSession.finalise(intent, AppIntegrityParameters(ATTESTATION, POP)) {}
        }
        // Then throw an IllegalArgumentException
        assertEquals(AuthenticationError.ErrorType.ACCESS_DENIED, result.type)
    }

    @Test
    fun deprecatedFinaliseThrowsAuthenticationErrorForIntentWithoutResponse() {
        // Given an (empty) intent without a response data JSON extra
        val intent = Intent()
        // When calling finalise
        val error = assertThrows(AuthenticationError::class.java) {
            appAuthSession.finalise(intent, AppIntegrityParameters(ATTESTATION, POP)) {}
        }
        // Then throw an AuthenticationError
        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
        assertEquals(AuthenticationError.Companion.NULL_AUTH_MESSAGE, error.message)
    }

    @Test
    fun deprecatedFinaliseThrowsAuthenticationErrorForIntentWithValidResponse() {
        // Given an intent without a response data JSON extra
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, authResponse)
        }
        // When calling finalise
        appAuthSession.finalise(intent, AppIntegrityParameters(ATTESTATION, POP)) {}
    }

    private fun buildTokenResponse(accessToken: String?): net.openid.appauth.TokenResponse {
        val clientId = "id"
        val tokenRequest = TokenRequest.Builder(
            AuthorizationServiceConfiguration(Uri.EMPTY, Uri.EMPTY),
            clientId
        ).setGrantType("grantType").build()
        return net.openid.appauth.TokenResponse.Builder(tokenRequest)
            .setRefreshToken(REFRESH_TOKEN)
            .setIdToken(ID_TOKEN)
            .setAccessToken(accessToken)
            .setTokenType(TOKEN_TYPE)
            .setAccessTokenExpirationTime(EXPIRATION_TIME)
            .build()
    }

    companion object {
        private const val ATTESTATION = "attestation"
        private const val POP = "proof of possession"
        private const val REFRESH_TOKEN = "refreshToken"
        private const val ID_TOKEN = "idToken"
        private const val ACCESS_TOKEN = "accessToken"
        private const val TOKEN_TYPE = "tokenType"
        private const val EXPIRATION_TIME = 123456789L
    }
}
