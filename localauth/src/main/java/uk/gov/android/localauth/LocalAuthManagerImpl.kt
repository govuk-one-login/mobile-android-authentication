package uk.gov.android.localauth

import androidx.fragment.app.FragmentActivity
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsManager
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsStatus
import uk.gov.android.localauth.preference.LocalAuthPreference
import uk.gov.android.localauth.preference.LocalAuthPreferenceRepository
import uk.gov.android.localauth.ui.BiometricsUiManager

class LocalAuthManagerImpl(
    private val localAuthPrefHandler: LocalAuthPreferenceRepository,
    private val deviceBiometricsManager: DeviceBiometricsManager,
) : LocalAuthManager {
    private val uiManager = BiometricsUiManager()
    private var _localAuthPreference: LocalAuthPreference? = localAuthPrefHandler.getLocalAuthPref()
    override val localAuthPreference: LocalAuthPreference?
        get() = _localAuthPreference

    override suspend fun enforceAndSet(
        localAuhRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        // Check if device is secure (any passcode and/ or any biometrics)
        if (deviceBiometricsManager.isDeviceSecure()) {
            when {
                // LocalAuthPref already set and saved (passcode/ biometrics) -- continue onSuccess
                (localAuthPreference is LocalAuthPreference.Enabled) -> callbackHandler.onSuccess()
                else -> {
                    // Go through the local auth flow
                    handleSecureDevice(callbackHandler, activity)
                }
            }
        } else {
            // When device does not have any passcode/ biometrics/ pattern/ etc
            // Check if device requires security
            handleUnsecuredDevice(localAuhRequired, activity, callbackHandler)
        }
    }

    private fun handleUnsecuredDevice(
        localAuhRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        if (localAuhRequired) {
            // Set pref to Disabled since there's no local auth and the user would now become a
            // returning user
            uiManager.displayGoToSettingsPage(
                activity = activity,
                onBack = {
                    // TODO: Additional behaviour (if required) + Analytics
                    callbackHandler.onFailure()
                    localAuthPrefHandler.setLocalAuthPref(LocalAuthPreference.Disabled)
                },
                onGoToSettings = {
                    callbackHandler.onFailure()
                    localAuthPrefHandler.setLocalAuthPref(LocalAuthPreference.Disabled)
                },
            )
        } else {
            // This is treated as success as it's not needed for the acton to be performed
            localAuthPrefHandler.setLocalAuthPref(LocalAuthPreference.Disabled)
            callbackHandler.onSuccess()
        }
    }

    private fun handleSecureDevice(
        callbackHandler: LocalAuthManagerCallbackHandler,
        activity: FragmentActivity,
    ) {
        when (deviceBiometricsManager.getCredentialStatus()) {
            DeviceBiometricsStatus.SUCCESS -> {
                uiManager.displayBioOptIn(
                    activity = activity,
                    onBack = {
                        localAuthPrefHandler.setLocalAuthPref(LocalAuthPreference.Enabled(false))
                        callbackHandler.onSuccess()
                        // TODO: Additional behaviour - if needed/ required + Analytics
                    },
                    onBiometricsOptIn = {
                        localAuthPrefHandler.setLocalAuthPref(LocalAuthPreference.Enabled(true))
                        callbackHandler.onSuccess()
                    },
                    onBiometricsOptOut = {
                        localAuthPrefHandler.setLocalAuthPref(LocalAuthPreference.Enabled(false))
                        callbackHandler.onSuccess()
                    },
                )
            }

            else -> {
                // Set passcode as default (this is enabled because of the .isDeviceSecure()
                // called above
                localAuthPrefHandler
                    .setLocalAuthPref(LocalAuthPreference.Enabled(false))
                callbackHandler.onSuccess()
            }
        }
    }
}
