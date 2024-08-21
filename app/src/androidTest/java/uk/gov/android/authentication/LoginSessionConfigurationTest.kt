package uk.gov.android.authentication

import android.net.Uri
import kotlin.test.assertEquals
import kotlin.test.Test

class LoginSessionConfigurationTest {
    @Test
    fun testDefaultLocaleIsEn() {
        val loginSessionConfiguration = LoginSessionConfiguration(
            authorizeEndpoint = Uri.parse("https://example.com/authorize"),
            clientId = "sampleClientId",
            redirectUri = Uri.parse("https://example.com/redirect"),
            scopes = listOf(LoginSessionConfiguration.Scope.OPENID),
            tokenEndpoint = Uri.parse("https://example.com/token")
        )
        assertEquals(LoginSessionConfiguration.Locale.EN, loginSessionConfiguration.locale)
    }

    @Test
    fun testResponseTypeIsCode() {
        val loginSessionConfiguration = LoginSessionConfiguration(
            Uri.parse("https://example.com/authorize"),
            "sampleClientId",
            redirectUri = Uri.parse("https://example.com/redirect"),
            scopes = listOf(LoginSessionConfiguration.Scope.OPENID),
            tokenEndpoint = Uri.parse("https://example.com/token")
        )
        assertEquals(
            LoginSessionConfiguration.ResponseType.CODE,
            loginSessionConfiguration.responseType
        )
    }

    @Test
    fun testVectorsOfTrustDefault() {
        val loginSessionConfiguration = LoginSessionConfiguration(
            Uri.parse("https://example.com/authorize"),
            "sampleClientId",
            redirectUri = Uri.parse("https://example.com/redirect"),
            scopes = listOf(LoginSessionConfiguration.Scope.OPENID),
            tokenEndpoint = Uri.parse("https://example.com/token")
        )
        assertEquals("[\"Cl.Cm.P0\"]", loginSessionConfiguration.vectorsOfTrust)
    }
}
