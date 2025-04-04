package uk.gov.android.localauth.devicesecurity

import android.app.KeyguardManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG

class DeviceBiometricsManagerImpl(
    private val biometricManager: BiometricManager,
    private val kgm: KeyguardManager,
) : DeviceBiometricsManager {
    override fun isDeviceSecure() = kgm.isDeviceSecure

    override fun getCredentialStatus(): DeviceBiometricsStatus =
        DeviceBiometricsStatus.fromBiometricManager(
            biometricManager.canAuthenticate(BIOMETRIC_STRONG),
        )
}
