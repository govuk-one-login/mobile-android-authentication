package uk.gov.android.localauth

import androidx.fragment.app.FragmentActivity
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsManager
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsStatus
import uk.gov.android.localauth.preference.LocalAuthPreference
import uk.gov.android.localauth.preference.LocalAuthPreferenceRepository
import uk.gov.android.localauth.ui.BiometricsUiManager
import uk.gov.logging.api.analytics.logging.AnalyticsLogger

/**
 * [LocalAuthManagerImpl] checks if the device is secure and based on that maps the local authentication preferences accordingly
 * and stores them in memory.
 *
 * @param localAuthPrefRepo Stores the [localAuthPreference]
 * @param deviceBiometricsManager Checks for device is secured and/ or biometrics are available and enabled
 * @param analyticsLogger Provides analytics logger for GA4 events for the [BiometricsUiManager]
 *
 * The functionality is based on the [enforceAndSet]:
 * * when [localAuthRequired] -> requires minimum of passcode/ pattern **enabled** on the
 * * when the device is secured, it checks if biometrics are available
 *
 * ** if yes, then it will display the [BioOptInScreen] which allows the user to set a preference for either biometrics or passcode/ pattern
 *
 * ** if no, then it will default to passcode as preference which will be stored and then referenced - this will only change if the device becomes unsecure
 *
 * * when the device is unsecure - if [localAuthRequired] is true, then it will land on the [GoToSettingScreen]
 * to enable any security on the device
 */
@Suppress("ForbiddenComment")
open class LocalAuthManagerImpl(
    private val localAuthPrefRepo: LocalAuthPreferenceRepository,
    private val deviceBiometricsManager: DeviceBiometricsManager,
    private val analyticsLogger: AnalyticsLogger,
) : LocalAuthManager {
    private val uiManager = BiometricsUiManager(analyticsLogger)
    override val localAuthPreference: LocalAuthPreference?
        get() = localAuthPrefRepo.getLocalAuthPref()

    @Deprecated(
        message = "Please use screen that does not allow for walletEnabled - will be removed 7th of March",
        level = DeprecationLevel.WARNING,
    )
    override suspend fun enforceAndSet(
        walletEnabled: Boolean,
        localAuthRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        // Check if device is secure (any passcode and/ or any biometrics)
        if (deviceBiometricsManager.isDeviceSecure()) {
            when {
                // LocalAuthPref already set and saved (passcode/ biometrics) -- continue onSuccess
                (localAuthPreference is LocalAuthPreference.Enabled)
                -> callbackHandler.onSuccess(false)

                else -> {
                    // Go through the local auth flow
                    handleSecureDevice(
                        callbackHandler,
                        activity,
                        walletEnabled,
                        localAuthRequired,
                    )
                }
            }
        } else {
            // When device does not have any passcode/ biometrics/ pattern/ etc
            // Check if device requires security
            handleUnsecuredDevice(localAuthRequired, activity, callbackHandler)
        }
    }

    override suspend fun enforceAndSet(
        localAuthRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        // Check if device is secure (any passcode and/ or any biometrics)
        if (deviceBiometricsManager.isDeviceSecure()) {
            when {
                // LocalAuthPref already set and saved (passcode/ biometrics) -- continue onSuccess
                (localAuthPreference is LocalAuthPreference.Enabled)
                -> callbackHandler.onSuccess(false)

                else -> {
                    // Go through the local auth flow
                    handleSecureDevice(
                        callbackHandler,
                        activity,
                        localAuthRequired,
                    )
                }
            }
        } else {
            // When device does not have any passcode/ biometrics/ pattern/ etc
            // Check if device requires security
            handleUnsecuredDevice(localAuthRequired, activity, callbackHandler)
        }
    }

    override fun biometricsAvailable(): Boolean {
        return deviceBiometricsManager.getCredentialStatus() == DeviceBiometricsStatus.SUCCESS
    }

    override fun toggleBiometrics() {
        // Check if device secured
        if (deviceBiometricsManager.isDeviceSecure()) {
            // If biometrics available but not saved as local auth preference
            if (biometricsAvailable() && localAuthPreference != LocalAuthPreference.Enabled(true)) {
                // Then save pref as biometrics
                localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Enabled(true))
            } else {
                // Otherwise save biometrics as false
                localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Enabled(false))
            }
        }
    }

    private fun handleUnsecuredDevice(
        localAuhRequired: Boolean,
        activity: FragmentActivity,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        if (localAuhRequired) {
            uiManager.displayGoToSettingsPage(
                activity = activity,
                onBack = {
                    localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Disabled)
                    callbackHandler.onFailure(true)
                },
                onGoToSettings = {
                    localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Disabled)
                    callbackHandler.onFailure(false)
                },
            )
        } else {
            // This is treated as success as it's not needed for the acton to be performed
            localAuthPrefRepo.setLocalAuthPref(LocalAuthPreference.Disabled)
            callbackHandler.onSuccess(false)
        }
    }

    @Deprecated(
        message = "Please use screen that does not allow for walletEnabled - will be removed 7th of March",
        level = DeprecationLevel.WARNING,
    )
    private fun handleSecureDevice(
        callbackHandler: LocalAuthManagerCallbackHandler,
        activity: FragmentActivity,
        walletEnabled: Boolean,
        isLocalAuthRequired: Boolean,
    ) {
        when (deviceBiometricsManager.getCredentialStatus()) {
            DeviceBiometricsStatus.SUCCESS -> {
                uiManager.displayBioOptIn(
                    activity = activity,
                    walletEnabled = walletEnabled,
                    onBack = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Disabled,
                        )
                        setLocalAuthBehaviour(isLocalAuthRequired, callbackHandler)
                    },
                    onBiometricsOptIn = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Enabled(true),
                        )
                        callbackHandler.onSuccess(false)
                    },
                    onBiometricsOptOut = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Disabled,
                        )
                        setOptOutLocalAuthBehaviour(
                            activity,
                            isLocalAuthRequired,
                            callbackHandler,
                        )
                    },
                )
            }

            else -> {
                // Set passcode as default (this is enabled because of the .isDeviceSecure()
                // called above
                localAuthPrefRepo
                    .setLocalAuthPref(LocalAuthPreference.Enabled(false))
                callbackHandler.onSuccess(false)
            }
        }
    }

    private fun handleSecureDevice(
        callbackHandler: LocalAuthManagerCallbackHandler,
        activity: FragmentActivity,
        isLocalAuthRequired: Boolean,
    ) {
        when (deviceBiometricsManager.getCredentialStatus()) {
            DeviceBiometricsStatus.SUCCESS -> {
                uiManager.displayBioOptIn(
                    activity = activity,
                    onBack = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Disabled,
                        )
                        setLocalAuthBehaviour(isLocalAuthRequired, callbackHandler)
                    },
                    onBiometricsOptIn = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Enabled(true),
                        )
                        callbackHandler.onSuccess(false)
                    },
                    onBiometricsOptOut = {
                        localAuthPrefRepo.setLocalAuthPref(
                            LocalAuthPreference.Disabled,
                        )
                        setOptOutLocalAuthBehaviour(
                            activity,
                            isLocalAuthRequired,
                            callbackHandler,
                        )
                    },
                )
            }

            else -> {
                // Set passcode as default (this is enabled because of the .isDeviceSecure()
                // called above
                localAuthPrefRepo
                    .setLocalAuthPref(LocalAuthPreference.Enabled(false))
                callbackHandler.onSuccess(false)
            }
        }
    }

    private fun setLocalAuthBehaviour(
        isLocalAuthRequired: Boolean,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        if (isLocalAuthRequired) {
            callbackHandler.onFailure(backButtonPressed = true)
        } else {
            callbackHandler.onSuccess(backButtonPressed = true)
        }
    }

    private fun setOptOutLocalAuthBehaviour(
        activity: FragmentActivity,
        isLocalAuthRequired: Boolean,
        callbackHandler: LocalAuthManagerCallbackHandler,
    ) {
        if (isLocalAuthRequired) {
            uiManager.displayBioOptOut(
                activity,
                onBack = {
                    callbackHandler.onFailure(backButtonPressed = true)
                },
                onBiometricsOptIn = {
                    localAuthPrefRepo.setLocalAuthPref(
                        LocalAuthPreference.Enabled(true),
                    )
                    callbackHandler.onSuccess(false)
                },
            )
        } else {
            callbackHandler.onSuccess(backButtonPressed = false)
        }
    }
}
