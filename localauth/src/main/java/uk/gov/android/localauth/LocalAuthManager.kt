package uk.gov.android.localauth

import androidx.fragment.app.FragmentActivity
import uk.gov.android.localauth.preference.LocalAuthPreference

/**
 * [LocalAuthManager] provides functionality for checking if a device is secured (using any form of
 * security such as password, pin, biometrics, etc) and saves/ updates the local authentication preference.
 *
 * [localAuthPreference] allows for the consumer to access the saved local authentication preference and
 * use it if required.
 *
 * For implementation details see [LocalAuthManagerImpl]
 */
interface LocalAuthManager {
    val localAuthPreference: LocalAuthPreference?

    /**
     * This method allows to checks if local authentication is available on the device and saves the
     * local auth preference.
     *
     * @param localAuhRequired enforces local authentication on the device - when this is set to true, it requires the device to be secure.
     * @param activity is required to allow the [BiometricsUiManager] to display dialogs on top of the consumer underlying activity
     * @param callbackHandler allows the consumer to provide implementation for success or failure results/ outcomes
     */
    suspend fun enforceAndSet(
        localAuhRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    )
}
