package uk.gov.android.authentication.login

import java.util.UUID
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationServiceConfiguration

internal fun LoginSessionConfiguration.createRequest(): AuthorizationRequest = createBuilder()
    .setScopes(scopeValues)
    .setUiLocales(locale.value)
    .setNonce(createNonce())
    .setAdditionalParameters(createAdditionalParameters())
    .build()

internal fun LoginSessionConfiguration.createBuilder() =
    AuthorizationRequest.Builder(
        createAuthorizationServiceConfiguration(),
        clientId,
        responseType.value,
        redirectUri
    )

internal fun LoginSessionConfiguration.createAuthorizationServiceConfiguration() =
    AuthorizationServiceConfiguration(
        authorizeEndpoint,
        tokenEndpoint
    )

internal val LoginSessionConfiguration.scopeValues: List<String>
    get() = scopes.map { it.value }

internal fun createNonce() = UUID.randomUUID().toString()

internal fun LoginSessionConfiguration.createAdditionalParameters(): Map<String, String?> {
    val params = mutableMapOf(VTR_PARAM_KEY to vectorsOfTrust)
    persistentSessionId?.let { params[SESSION_ID_PARAM_KEY] = it }
    return params
}

internal const val VTR_PARAM_KEY = "vtr"
internal const val SESSION_ID_PARAM_KEY = "govuk_signin_session_id"
