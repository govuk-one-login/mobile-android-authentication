/*
 * This file is based on work under the Apache License, Version 2.0.
 * Original code is by OpenId and can be found at https://github.com/openid/AppAuth-Android.
 *
 * Modifications made by ThomasIent on the twentieth of August, 2024.
 * - Concatenated TestValues and AuthorisationServiceDiscoveryTest
 * - Converted from Java to Kotlin
 * - Reduced code to what's required for internal test usage
 *
 * Note this file is used for internal testing purposes only and is not for distribution.
 */
package uk.gov.android.authentication.login.openid

import android.net.Uri
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.AuthorizationServiceDiscovery
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object TestValues {
    const val TEST_CLIENT_ID: String = "test_client_id"
    private const val TEST_APP_SCHEME: String = "com.test.app"
    val TEST_APP_REDIRECT_URI: Uri = Uri.parse(TEST_APP_SCHEME + ":/oidc_callback")
    const val TEST_AUTH_CODE: String = "zxcvbnmjk"
    private const val TEST_ISSUER: String = "https://test.issuer"
    private const val TEST_AUTHORIZATION_ENDPOINT = "http://test.openid.com/o/oauth/auth"
    private const val TEST_TOKEN_ENDPOINT = "http://test.openid.com/o/oauth/token"
    private const val TEST_USERINFO_ENDPOINT = "http://test.openid.com/o/oauth/userinfo"
    private const val TEST_REGISTRATION_ENDPOINT = "http://test.openid.com/o/oauth/register"
    private const val TEST_END_SESSION_ENDPOINT = "http://test.openid.com/o/oauth/logout"
    private const val TEST_JWKS_URI = "http://test.openid.com/o/oauth/jwks"

    private val TEST_RESPONSE_TYPES_SUPPORTED: List<String> = mutableListOf("code", "token")
    private val TEST_SUBJECT_TYPES_SUPPORTED: List<String> = mutableListOf("public")
    private val TEST_ID_TOKEN_SIGNING_ALG_VALUES: List<String> = mutableListOf("RS256")
    private val TEST_SCOPES_SUPPORTED: List<String> = mutableListOf("openid", "profile")
    private val TEST_TOKEN_ENDPOINT_AUTH_METHODS: List<String> =
        mutableListOf("client_secret_post", "client_secret_basic")
    private val TEST_CLAIMS_SUPPORTED: List<String> = mutableListOf("aud", "exp")
    private val TEST_JSON: String = getDiscoveryDocumentJson()

    private fun getDiscoveryDocumentJson(): String {
        return (
            (
                "{\n" +
                    " \"issuer\": \"" + TEST_ISSUER + "\",\n" +
                    " \"authorization_endpoint\": \"" + TEST_AUTHORIZATION_ENDPOINT + "\",\n" +
                    " \"token_endpoint\": \"" + TEST_TOKEN_ENDPOINT + "\",\n" +
                    " \"userinfo_endpoint\": \"" + TEST_USERINFO_ENDPOINT + "\",\n" +
                    " \"end_session_endpoint\": \"" + TEST_END_SESSION_ENDPOINT + "\",\n" +
                    " \"registration_endpoint\": \"" + TEST_REGISTRATION_ENDPOINT + "\",\n" +
                    " \"jwks_uri\": \"" + TEST_JWKS_URI + "\",\n" +
                    " \"response_types_supported\": " +
                    toJson(TEST_RESPONSE_TYPES_SUPPORTED) + ",\n" +
                    " \"subject_types_supported\": " +
                    toJson(TEST_SUBJECT_TYPES_SUPPORTED) + ",\n" +
                    " \"id_token_signing_alg_values_supported\": " +
                    toJson(TEST_ID_TOKEN_SIGNING_ALG_VALUES) + ",\n" +
                    " \"scopes_supported\": " + toJson(TEST_SCOPES_SUPPORTED) + ",\n" +
                    " \"token_endpoint_auth_methods_supported\": " +
                    toJson(TEST_TOKEN_ENDPOINT_AUTH_METHODS) + ",\n" +
                    " \"claims_supported\": " + toJson(TEST_CLAIMS_SUPPORTED) + "\n" +
                    "}"
                )
            )
    }

    private fun toJson(strings: List<String?>): String = JSONArray(strings).toString()

    @Suppress("TooGenericExceptionThrown")
    private val testDiscoveryDocument: AuthorizationServiceDiscovery
        get() {
            try {
                return AuthorizationServiceDiscovery(
                    JSONObject(TEST_JSON)
                )
            } catch (ex: JSONException) {
                throw RuntimeException(
                    "Unable to create test authorization service discover document",
                    ex
                )
            } catch (ex: AuthorizationServiceDiscovery.MissingArgumentException) {
                throw RuntimeException(
                    "Unable to create test authorization service discover document",
                    ex
                )
            }
        }

    val testServiceConfig: AuthorizationServiceConfiguration
        get() = AuthorizationServiceConfiguration(testDiscoveryDocument)
}
