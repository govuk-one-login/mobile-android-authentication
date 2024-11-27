package uk.gov.android.authentication.login

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.test.platform.app.InstrumentationRegistry
import net.openid.appauth.AuthorizationResponse
import org.junit.Assert.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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
    private lateinit var clientAuthenticationProvider: ClientAuthenticationProvider

    @BeforeTest
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        clientAuthenticationProvider = ClientAuthenticationProviderImpl()
        appAuthSession = AppAuthSession(context, clientAuthenticationProvider)
    }

    @Test
    fun presentCreatesAndLaunchesAuthorisationIntent() {
        // Given a mock ActivityResultLauncher
        val launcher: ActivityResultLauncher<Intent> = mock()
        whenever(launcher.launch(any())).then {  } // take no action
        val loginSessionConfig = LoginSessionConfigurationTest.defaultConfig.copy()
        // When calling present
        appAuthSession.present(
            launcher = launcher,
            configuration = loginSessionConfig
        )
        // Then create the AuthorisationIntent and launch
        verify(launcher).launch(any())
    }

    @Test(expected = IllegalArgumentException::class)
    fun finaliseThrowsIllegalArgumentExceptionForMalformedIntentResponse() {
        // Given an intent with a malformed (empty) response data JSON extra
        val intent = Intent().apply {
            putExtra(AuthorizationResponse.EXTRA_RESPONSE, "{}")
        }
        // When calling finalise
        appAuthSession.finalise(intent, Pair(ATTESTATION, POP)) {}
        // Then throw an IllegalArgumentException
    }

    @Test
    fun finaliseThrowsAuthenticationErrorForIntentWithoutResponse() {
        // Given an (empty) intent without a response data JSON extra
        val intent = Intent()
        // When calling finalise
        val error = assertThrows(AuthenticationError::class.java) {
            appAuthSession.finalise(intent, Pair(ATTESTATION, POP)) {}
        }
        // Then throw an AuthenticationError
        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
        println("${AuthenticationError.Companion.NULL_AUTH_MESSAGE}, ${error.message}")
        assertEquals(AuthenticationError.Companion.NULL_AUTH_MESSAGE, error.message)
    }

    companion object {
        private const val ATTESTATION = "attestation"
        private const val POP = "proof of possession"
    }
}
