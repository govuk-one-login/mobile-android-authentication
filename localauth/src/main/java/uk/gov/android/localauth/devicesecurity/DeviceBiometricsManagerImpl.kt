package uk.gov.android.localauth.devicesecurity

import android.app.KeyguardManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK

class DeviceBiometricsManagerImpl(
    private val biometricManager: BiometricManager,
    private val kgm: KeyguardManager,
) : DeviceBiometricsManager {
    override fun isDeviceSecure() = kgm.isDeviceSecure

    /**
     * BIOMETRIC_WEAK is a superset of BIOMETRIC_STRONG, so a single check covers strong
     * (e.g. fingerprint) and weak (e.g. Class 2 face unlock) biometrics with one status.
     */
    override fun getCredentialStatus(): DeviceBiometricsStatus =
        DeviceBiometricsStatus.fromBiometricManager(
            biometricManager.canAuthenticate(BIOMETRIC_WEAK),
        )
}
