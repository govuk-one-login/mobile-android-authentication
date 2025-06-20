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
     * @param walletEnabled if the consumer has access to Wallet, it is used to display the correct [BioOptInScreen] copy
     * @param localAuthRequired enforces local authentication on the device - when this is set to true, it requires the device to be secure.
     * @param enableOptOut indicates whether the opt out screen should be displayed when user skips biometrics opt in
     * @param activity is required to allow the [BiometricsUiManager] to display dialogs on top of the consumer underlying activity
     * @param callbackHandler allows the consumer to provide implementation for success or failure results/ outcomes
     */
    suspend fun enforceAndSet(
        walletEnabled: Boolean,
        localAuthRequired: Boolean,
        enableOptOut: Boolean = false,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    )

    /**
     * This method allows to check if biometrics are available and enabled on the device
     *
     * @return **true** if the device has the hardware and the user enabled this (existing biometrics on device) and **false** if biometrics are **NOT** enabled or the device does not support it
     */
    fun biometricsAvailable(): Boolean

    /**
     * This method allows to change the biometrics preference from enabled to disabled, and otherwise.
     */
    fun toggleBiometrics()
}
