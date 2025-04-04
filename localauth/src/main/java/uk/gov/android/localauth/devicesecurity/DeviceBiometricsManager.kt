package uk.gov.android.localauth.devicesecurity

interface DeviceBiometricsManager {
    fun isDeviceSecure(): Boolean

    fun getCredentialStatus(): DeviceBiometricsStatus
}
