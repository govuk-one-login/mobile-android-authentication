package uk.gov.android.localauth.devicesecurity

/**
 * This allows to check if any form of security (e.g: password, pin, biometrics, etc) is enabled on
 * the device. It also provides functionality to check if biometrics are available on the device and
 * enabled.
 */
interface DeviceBiometricsManager {
    /**
     * Checks the device security is enabled.
     */
    fun isDeviceSecure(): Boolean

    /**
     * Checks is biometrics are available and maps the result to [DeviceBiometricsStatus].
     */
    fun getCredentialStatus(): DeviceBiometricsStatus
}
