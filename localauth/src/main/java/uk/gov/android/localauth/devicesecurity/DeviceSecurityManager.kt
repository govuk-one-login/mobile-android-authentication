package uk.gov.android.localauth.devicesecurity

interface DeviceSecurityManager {
    fun isDeviceSecure(): Boolean

    fun getCredentialStatus(): DeviceSecurityStatus
}
