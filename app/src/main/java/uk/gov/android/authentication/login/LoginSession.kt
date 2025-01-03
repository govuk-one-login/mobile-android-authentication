package uk.gov.android.authentication.login

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import uk.gov.android.authentication.integrity.AppIntegrityParameters

/**
 * This class provides a wrapper for [net.openid.appauth](https://github.com/openid/AppAuth-Android)
 *
 * Use to make OAuth standard login
 */
interface LoginSession {
    /**
     * Present the login dialog using a Chrome Custom Tab
     *
     * @param launcher [ActivityResultLauncher] a launcher to start the Login activity for result
     * @param configuration [LoginSessionConfiguration] containing necessary session configuration
     */
    fun present(
        launcher: ActivityResultLauncher<Intent>,
        configuration: LoginSessionConfiguration
    )

    /**
     * Callback function to handle intent from the activity result started by [present]
     *
     * @param intent The intent from the login activity result
     * @param appIntegrity Provides a ClientAttestation JWT and PoP JWT to be attached in the auth request header parameters
     * @param callback Method to extract and handle local token usage/storage
     * @throws [AuthenticationError] if Authorization fails
     */
    @Throws(Exception::class)
    fun finalise(
        intent: Intent,
        appIntegrity: AppIntegrityParameters,
        callback: (tokens: TokenResponse) -> Unit
    )
}
