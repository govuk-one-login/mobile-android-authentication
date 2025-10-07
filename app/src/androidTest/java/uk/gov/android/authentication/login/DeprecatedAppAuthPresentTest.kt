package uk.gov.android.authentication.login

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.intent.matcher.UriMatchers
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import net.openid.appauth.AuthorizationManagementActivity
import org.hamcrest.CoreMatchers.not
import uk.gov.android.authentication.TestActivity

class DeprecatedAppAuthPresentTest {
    private lateinit var loginSession: LoginSession
    private lateinit var loginSessionConfig: LoginSessionConfiguration
    private lateinit var clientAuthenticationProvider: ClientAuthenticationProvider

    @BeforeTest
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context
        clientAuthenticationProvider = ClientAuthenticationProviderImpl()
        loginSession = AppAuthSession(context)
        loginSessionConfig = LoginSessionConfigurationTest.defaultConfig.copy()
        Intents.init()
    }

    @AfterTest
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun presentLaunchesAuthorizationManagementActivity() {
        // Given a registered ActivityResultLauncher
        val scenario = ActivityScenario.launch(TestActivity::class.java)
        intending(not(isInternal())).respondWith(
            Instrumentation.ActivityResult(Activity.RESULT_OK, null)
        )
        scenario.onActivity { activity ->
            val launcher = (activity as TestActivity).launcher
            // When calling present()
            loginSession.present(launcher, loginSessionConfig)
        }

        // Then launch an AuthorizationManagementActivity intent
        Intents.intended(
            IntentMatchers.hasComponent(AuthorizationManagementActivity::class.java.name)
        )
        // Aimed at the host, path, and scheme set by the LoginSessionConfiguration.authorizeEndpoint
        val host = loginSessionConfig.authorizeEndpoint.host
        val path = loginSessionConfig.authorizeEndpoint.path
        val scheme = loginSessionConfig.authorizeEndpoint.scheme
        Intents.intended(IntentMatchers.hasData(UriMatchers.hasHost(host)))
        Intents.intended(IntentMatchers.hasData(UriMatchers.hasPath(path)))
        Intents.intended(IntentMatchers.hasData(UriMatchers.hasScheme(scheme)))
    }
}
