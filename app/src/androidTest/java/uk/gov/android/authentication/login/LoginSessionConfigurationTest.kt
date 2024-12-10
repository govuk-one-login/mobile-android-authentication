package uk.gov.android.authentication.login

import android.net.Uri
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginSessionConfigurationTest {
    @Test
    fun testDefaultLocaleIsEn() {
        // Given the default LoginSessionConfiguration construction
        val loginSessionConfiguration = defaultConfig.copy()
        // Then the default Locale is Locale.EN
        assertEquals(LoginSessionConfiguration.Locale.EN, loginSessionConfiguration.locale)
    }

    @Test
    fun testResponseTypeIsCode() {
        // Given the default LoginSessionConfiguration construction
        val loginSessionConfiguration = defaultConfig.copy()
        // Then the default ResponseType is ResponseTypeCODE
        assertEquals(
            LoginSessionConfiguration.ResponseType.CODE,
            loginSessionConfiguration.responseType
        )
    }

    @Test
    fun testVectorsOfTrustDefault() {
        // Given the default LoginSessionConfiguration construction
        val loginSessionConfiguration = defaultConfig.copy()
        // Then the default ResponseType is ResponseTypeCODE
        assertEquals(
            LoginSessionConfiguration.VTR_DEFAULT,
            loginSessionConfiguration.vectorsOfTrust
        )
    }

    companion object {
        val defaultConfig = LoginSessionConfiguration(
            authorizeEndpoint = Uri.parse("https://token.build.account.gov.uk/authorize"),
            clientId = "WCrqGT_3zu62gdo0WktPnkBPVK8",
            redirectUri = Uri.parse("https://mobile.build.account.gov.uk/wallet-test/redirect"),
            scopes = listOf(LoginSessionConfiguration.Scope.OPENID),
            tokenEndpoint = Uri.parse("https://token.build.account.gov.uk/token")
        )
    }
}
