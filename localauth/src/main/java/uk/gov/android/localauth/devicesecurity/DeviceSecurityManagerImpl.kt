package uk.gov.android.localauth.devicesecurity

import android.app.KeyguardManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG

class DeviceSecurityManagerImpl(
    private val biometricManager: BiometricManager,
    private val kgm: KeyguardManager
): DeviceSecurityManager {
    override fun isDeviceSecure() = kgm.isDeviceSecure

    override fun getCredentialStatus(): DeviceSecurityStatus =
        DeviceSecurityStatus.fromBiometricManager(
            biometricManager.canAuthenticate(BIOMETRIC_STRONG)
        )
}
