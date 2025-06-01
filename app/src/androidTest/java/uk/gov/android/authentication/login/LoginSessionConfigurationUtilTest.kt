package uk.gov.android.authentication.login

import android.net.Uri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LoginSessionConfigurationUtilTest {
    private val scopes =
        listOf(
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

    @BeforeTest
    fun setUp() {
        loginSessionConfig =
            LoginSessionConfiguration(
                authorizeEndpoint = authorizeEndpoint,
                clientId = clientId,
                locale = locale,
                prefersEphemeralWebSession = true,
                redirectUri = redirectUri,
                responseType = responseType,
                scopes = scopes,
                tokenEndpoint = tokenEndpoint,
                vectorsOfTrust = vectorsOfTrust,
                persistentSessionId = persistentSessionId,
            )
    }

    @Test
    fun createRequest() {
        // Given a LoginSessionConfiguration
        // When calling the createRequest() extension function
        val actual = loginSessionConfig.createRequest()
        // Then return the AuthorizationRequest with the parameters set
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
        // Given a login session configuration
        // When calling the createAuthorizationServiceConfiguration() extension function
        val actual = loginSessionConfig.createAuthorizationServiceConfiguration()
        // Then return the AuthorizationServiceConfiguration with authorizationEndpoint and tokenEndpoint set
        assertIs<AuthorizationServiceConfiguration>(actual)
        assertEquals(authorizeEndpoint, actual.authorizationEndpoint)
        assertEquals(tokenEndpoint, actual.tokenEndpoint)
    }

    @Test
    fun scopeValuesReturnsStringListOfScopeValues() {
        // Given a LoginSessionConfiguration
        // When calling the LoginSessionConfiguration.scopeValues extension property
        val actual = loginSessionConfig.scopeValues
        // Then return the List of String values of the LoginSessionConfiguration.Scopes
        assertIs<List<String>>(actual)
        assertContains(actual, LoginSessionConfiguration.Scope.OPENID.value)
        assertContains(actual, LoginSessionConfiguration.Scope.PHONE.value)
        assertContains(actual, LoginSessionConfiguration.Scope.EMAIL.value)
    }

    @Test
    fun createNonceReturnsStringNonce() {
        // Given
        // When calling the createNonce() factory function
        val actual = createNonce()
        // Then return a String?
        // Todo(): This is currently a weak test, at least check format
        assertIs<String>(actual)
    }

    @Test
    fun createAdditionalParametersReturnsMapOfVectorsOfTrustAndPersistentSessionId() {
        // Given a login session configuration with vectorsOfTrust and persistentSessionId set
        // When calling the createAdditionalParameters extension function
        val actual = loginSessionConfig.createAdditionalParameters()
        // Then map vectorsOfTrust and persistentSessionId to their respective Keys
        assertIs<Map<String, String?>>(actual)
        assertTrue(actual.containsKey(VTR_PARAM_KEY))
        assertEquals(vectorsOfTrust, actual[VTR_PARAM_KEY])
        assertTrue(actual.containsKey(SESSION_ID_PARAM_KEY))
        assertEquals(persistentSessionId, actual[SESSION_ID_PARAM_KEY])
    }

    @Test
    fun createAdditionalParametersReturnsWithoutPersistentSessionIdKeyWhenNull() {
        // Given a login session configuration with vectorsOfTrust set and null persistentSessionId
        loginSessionConfig = loginSessionConfig.copy(persistentSessionId = null)
        // When calling the createAdditionalParameters extension function
        val actual = loginSessionConfig.createAdditionalParameters()
        // Then only map vectorsOfTrust to its respective Key
        assertIs<Map<String, String?>>(actual)
        assertTrue(actual.containsKey(VTR_PARAM_KEY))
        assertEquals(vectorsOfTrust, actual[VTR_PARAM_KEY])
        assertFalse(actual.containsKey(SESSION_ID_PARAM_KEY))
    }
}
