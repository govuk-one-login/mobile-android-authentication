package uk.gov.android.authentication.login

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.test.platform.app.InstrumentationRegistry
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import net.openid.appauth.AuthorizationResponse
import org.junit.Assert.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.android.authentication.integrity.AppIntegrityParameters
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/* Not checked here:
* Full route of AppAuthSession::present
* Full route of AppAuthSession::finalise
* Because the real AuthorizationService has to have a browser available from the context
*  */
class AppAuthSessionTest {
    private lateinit var appAuthSession: AppAuthSession
    private var authResponse =
        "{\n" +
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

    @BeforeTest
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        appAuthSession = AppAuthSession(context)
    }

    @Test
    fun presentCreatesAndLaunchesAuthorisationIntent() {
        // Given a mock ActivityResultLauncher
        val launcher: ActivityResultLauncher<Intent> = mock()
        whenever(launcher.launch(any())).then { } // take no action
        val loginSessionConfig = LoginSessionConfigurationTest.defaultConfig.copy()
        // When calling present
        appAuthSession.present(
            launcher = launcher,
            configuration = loginSessionConfig,
        )
        // Then create the AuthorisationIntent and launch
        verify(launcher).launch(any())
    }

    @Test(expected = IllegalArgumentException::class)
    fun finaliseThrowsIllegalArgumentExceptionForMalformedIntentResponse() {
        // Given an intent with a malformed (empty) response data JSON extra
        val intent =
            Intent().apply {
                putExtra(AuthorizationResponse.EXTRA_RESPONSE, "{}")
            }
        // When calling finalise
        appAuthSession.finalise(intent, AppIntegrityParameters(ATTESTATION, POP)) {}
        // Then throw an IllegalArgumentException
    }

    @Test
    fun finaliseThrowsAuthenticationErrorOfAccessDenied() {
        val exception =
            AuthorizationException(
                AuthorizationException.TYPE_OAUTH_AUTHORIZATION_ERROR,
                AuthorizationRequestErrors.ACCESS_DENIED.code,
                AuthorizationRequestErrors.ACCESS_DENIED.error,
                AuthorizationRequestErrors.ACCESS_DENIED.errorDescription,
                AuthorizationRequestErrors.ACCESS_DENIED.errorUri,
                AuthorizationRequestErrors.ACCESS_DENIED.cause,
            )
        // Given an intent with a malformed (empty) response data JSON extra
        val intent =
            Intent().putExtra(
                AuthorizationException.EXTRA_EXCEPTION,
                exception.toJsonString(),
            )
        // When calling finalise
        val result =
            assertThrows(AuthenticationError::class.java) {
                appAuthSession.finalise(intent, AppIntegrityParameters(ATTESTATION, POP)) {}
            }
        // Then throw an IllegalArgumentException
        assertEquals(AuthenticationError.ErrorType.ACCESS_DENIED, result.type)
    }

    @Test
    fun finaliseThrowsAuthenticationErrorForIntentWithoutResponse() {
        // Given an (empty) intent without a response data JSON extra
        val intent = Intent()
        // When calling finalise
        val error =
            assertThrows(AuthenticationError::class.java) {
                appAuthSession.finalise(intent, AppIntegrityParameters(ATTESTATION, POP)) {}
            }
        // Then throw an AuthenticationError
        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
        assertEquals(AuthenticationError.Companion.NULL_AUTH_MESSAGE, error.message)
    }

    @Test
    fun finaliseThrowsAuthenticationErrorForIntentWithValidResponse() {
        // Given an intent without a response data JSON extra
        val intent =
            Intent().apply {
                putExtra(AuthorizationResponse.EXTRA_RESPONSE, authResponse)
            }
        // When calling finalise
        appAuthSession.finalise(intent, AppIntegrityParameters(ATTESTATION, POP)) {}
    }

    companion object {
        private const val ATTESTATION = "attestation"
        private const val POP = "proof of possession"
    }
}
