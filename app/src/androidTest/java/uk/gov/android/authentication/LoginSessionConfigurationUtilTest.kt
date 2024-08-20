package uk.gov.android.authentication

import android.net.Uri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LoginSessionConfigurationUtilTest {
    private val scopes = listOf(
        LoginSessionConfiguration.Scope.OPENID,
        LoginSessionConfiguration.Scope.PHONE,
        LoginSessionConfiguration.Scope.EMAIL,
    )
    private val locale = LoginSessionConfiguration.Locale.EN
    private val clientId = "clientId.Test"
    private val responseType = LoginSessionConfiguration.ResponseType.CODE
    private val redirectUri = Uri.parse("https://redirect.gov.uk/test")
    private val authorizeEndpoint = Uri.parse("https://auth.gov.uk/test")
    private val tokenEndpoint = Uri.parse("https://token.gov.uk/test")
    private val vectorsOfTrust: String = "[\"Cl.Cm.P0\"]"
    private val persistentSessionId: String = "persistentSessionTestId"
    private lateinit var loginSessionConfig: LoginSessionConfiguration

    @Before
    fun setUp() {
        loginSessionConfig = LoginSessionConfiguration(
            authorizeEndpoint = authorizeEndpoint,
            clientId = clientId,
            locale = locale,
            prefersEphemeralWebSession = true,
            redirectUri = redirectUri,
            responseType = responseType,
            scopes = scopes,
            tokenEndpoint = tokenEndpoint,
            vectorsOfTrust = vectorsOfTrust,
            persistentSessionId = persistentSessionId
        )
    }

    @Test
    fun createRequest() {
        // GIVEN a login session configuration
        // WHEN the createRequest extension function is called
        val actual = loginSessionConfig.createRequest()
        // THEN the AuthorizationRequest is returned with the parameters set
        assertIs<AuthorizationRequest>(actual)
        assertEquals(loginSessionConfig.scopeValues.toSet(), actual.scopeSet)
        assertEquals(locale.value, actual.uiLocales)
        assertTrue(actual.additionalParameters.isNotEmpty())
        assertEquals(clientId, actual.clientId)
        assertEquals(responseType.value, actual.responseType)
        assertEquals(redirectUri, actual.redirectUri)
    }

    @Test
    fun createAuthorizationServiceConfiguration() {
        // GIVEN a login session configuration
        // WHEN the createAuthorizationServiceConfiguration extension function is called
        val actual = loginSessionConfig.createAuthorizationServiceConfiguration()
        // THEN the AuthorizationServiceConfiguration is returned with authorizationEndpoint and tokenEndpoint set
        assertIs<AuthorizationServiceConfiguration>(actual)
        assertEquals(authorizeEndpoint, actual.authorizationEndpoint)
        assertEquals(tokenEndpoint, actual.tokenEndpoint)
    }

    @Test
    fun scopeValuesReturnsStringListOfScopeValues() {
        // GIVEN a LoginSessionConfiguration
        // WHEN the scopeValues extension parameter is called
        val actual = loginSessionConfig.scopeValues
        // THEN a List of String values of the LoginSessionConfiguration.Scopes is returned
        assertIs<List<String>>(actual)
        assertContains(actual, LoginSessionConfiguration.Scope.OPENID.value)
        assertContains(actual, LoginSessionConfiguration.Scope.PHONE.value)
        assertContains(actual, LoginSessionConfiguration.Scope.EMAIL.value)
    }

    @Test
    fun createNonceReturnsStringNonce() {
        // GIVEN
        // WHEN the createNonce factory function is called
        val actual = createNonce()
        // THEN a String is returned?
        // Todo: This is currently a weak test, at least check format!
        assertIs<String>(actual)
    }

    @Test
    fun createAdditionalParametersReturnsMapOfVectorsOfTrustAndPersistentSessionId() {
        // GIVEN a login session configuration with vectorsOfTrust and persistentSessionId set
        // WHEN the createAdditionalParameters extension function is called
        val actual = loginSessionConfig.createAdditionalParameters()
        // THEN vectorsOfTrust and persistentSessionId are returned mapped to their respective Keys
        assertIs<Map<String, String?>>(actual)
        assertTrue(actual.containsKey(VTR_PARAM_KEY))
        assertEquals(vectorsOfTrust, actual[VTR_PARAM_KEY])
        assertTrue(actual.containsKey(SESSION_ID_PARAM_KEY))
        assertEquals(persistentSessionId, actual[SESSION_ID_PARAM_KEY])
    }

    @Test
    fun createAdditionalParametersReturnsWithoutPersistentSessionIdKeyWhenNull() {
        // GIVEN a login session configuration with vectorsOfTrust set and null persistentSessionId
        loginSessionConfig = loginSessionConfig.copy(persistentSessionId = null)
        // WHEN the createAdditionalParameters extension function is called
        val actual = loginSessionConfig.createAdditionalParameters()
        // THEN only vectorsOfTrust is returned mapped to its respective Key
        assertIs<Map<String, String?>>(actual)
        assertTrue(actual.containsKey(VTR_PARAM_KEY))
        assertEquals(vectorsOfTrust, actual[VTR_PARAM_KEY])
        assertFalse(actual.containsKey(SESSION_ID_PARAM_KEY))
    }
}
