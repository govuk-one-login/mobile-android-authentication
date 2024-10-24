package uk.gov.android.authentication.login

import android.net.Uri

/**
 * Class to bundle required session configuration
 *
 */
data class LoginSessionConfiguration(
    val authorizeEndpoint: Uri,
    val clientId: String,
    val locale: Locale = Locale.EN,
    val prefersEphemeralWebSession: Boolean = true,
    val redirectUri: Uri,
    val responseType: ResponseType = ResponseType.CODE,
    val scopes: List<Scope>,
    val tokenEndpoint: Uri,
    val vectorsOfTrust: String = VTR_DEFAULT,
    val persistentSessionId: String? = null
) {
    enum class ResponseType(val value: String) {
        CODE("code"),
    }

    enum class Scope(val value: String) {
        OPENID("openid"),
        EMAIL("email_address"),
        PHONE("phone_number"),
        OFFLINE_ACCESS("offline_access"),
        STS("sts")
    }

    enum class Locale(val value: String) {
        EN("en"),
        CY("cy")
    }

    companion object {
        internal const val VTR_DEFAULT = "[\"Cl.Cm.P0\"]"
    }
}
