package uk.gov.android.localauth.device

import android.app.KeyguardManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsManager
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsManagerImpl
import uk.gov.android.localauth.devicesecurity.DeviceBiometricsStatus
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceBiometricsManagerTest {
    private lateinit var biometricsManager: BiometricManager
    private lateinit var kgm: KeyguardManager
    private lateinit var deviceBiometricsManager: DeviceBiometricsManager

    @BeforeEach
    fun setup() {
        biometricsManager = mock()
        kgm = mock()
        deviceBiometricsManager = DeviceBiometricsManagerImpl(biometricsManager, kgm)
    }

    @Test
    fun `device is secure`() {
        whenever(kgm.isDeviceSecure).thenReturn(true)
        assertTrue(deviceBiometricsManager.isDeviceSecure())
    }

    @Test
    fun `device is not secure`() {
        whenever(kgm.isDeviceSecure).thenReturn(false)
        assertFalse(deviceBiometricsManager.isDeviceSecure())
    }

    @Test
    fun `biometrics available and enabled`() {
        whenever(biometricsManager.canAuthenticate(eq(BIOMETRIC_STRONG)))
            .thenReturn(BiometricManager.BIOMETRIC_SUCCESS)
        val result = deviceBiometricsManager.getCredentialStatus()
        assertEquals(DeviceBiometricsStatus.SUCCESS, result)
    }

    @Test
    fun `biometrics unknown`() {
        whenever(biometricsManager.canAuthenticate(eq(BIOMETRIC_STRONG)))
            .thenReturn(BiometricManager.BIOMETRIC_STATUS_UNKNOWN)
        val result = deviceBiometricsManager.getCredentialStatus()
        assertEquals(DeviceBiometricsStatus.UNKNOWN, result)
    }

    @Test
    fun `no hardware for biometrics`() {
        whenever(biometricsManager.canAuthenticate(eq(BIOMETRIC_STRONG)))
            .thenReturn(BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE)
        val result = deviceBiometricsManager.getCredentialStatus()
        assertEquals(DeviceBiometricsStatus.NO_HARDWARE, result)
    }

    @Test
    fun `biometrics hardware unavailable`() {
        whenever(biometricsManager.canAuthenticate(eq(BIOMETRIC_STRONG)))
            .thenReturn(BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE)
        val result = deviceBiometricsManager.getCredentialStatus()
        assertEquals(DeviceBiometricsStatus.HARDWARE_UNAVAILABLE, result)
    }

    @Test
    fun `biometrics not enrolled`() {
        whenever(biometricsManager.canAuthenticate(eq(BIOMETRIC_STRONG)))
            .thenReturn(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
        val result = deviceBiometricsManager.getCredentialStatus()
        assertEquals(DeviceBiometricsStatus.NOT_ENROLLED, result)
    }
}
