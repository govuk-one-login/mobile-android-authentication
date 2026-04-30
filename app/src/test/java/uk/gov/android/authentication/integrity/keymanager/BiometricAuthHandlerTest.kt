package uk.gov.android.authentication.integrity.keymanager

import android.os.Build
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler.AccessControlLevel

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.R])
class BiometricAuthHandlerTest {
    private val activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun `authenticate on SDK below R uses legacy biometric configuration`() {
        // GIVEN
        val handler = BiometricAuthHandler(activity)
        val request =
            BiometricAuthHandler.Request(
                accessControlLevel = AccessControlLevel.PASSCODE,
                promptConfig = BiometricAuthHandler.PromptConfig("Title", "Close"),
                callback = BiometricAuthHandler.Callback()
            )

        // WHEN
        handler.authenticate(request)

        // THEN
        // Verify no exception thrown - legacy path executed
    }

    @Test
    fun `authenticate throws exception when access control level is OPEN`() {
        // GIVEN
        val handler = BiometricAuthHandler(activity)
        val request =
            BiometricAuthHandler.Request(
                accessControlLevel = AccessControlLevel.OPEN,
                promptConfig = BiometricAuthHandler.PromptConfig("Title", "Close"),
                callback = BiometricAuthHandler.Callback()
            )

        // WHEN & THEN
        assertThrows<IllegalArgumentException> {
            handler.authenticate(request)
        }
    }

    @Test
    fun `PASSCODE returns correct authenticator`() {
        assertEquals(DEVICE_CREDENTIAL, AccessControlLevel.PASSCODE.toAuthenticators())
    }

    @Test
    fun `PASSCODE_AND_BIOMETRICS returns correct authenticator`() {
        assertEquals(
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL,
            AccessControlLevel.PASSCODE_AND_BIOMETRICS.toAuthenticators()
        )
    }

    @Test
    fun `OPEN returns -1`() {
        assertEquals(-1, AccessControlLevel.OPEN.toAuthenticators())
    }

    @Test
    fun `onAuthenticationError with ERROR_UNABLE_TO_PROCESS calls onFailure`() {
        // GIVEN
        var failureCalled = false
        var errorCalled = false
        val callback =
            BiometricAuthHandler.Callback(
                onFailure = { failureCalled = true },
                onError = { _, _ -> errorCalled = true }
            )

        // WHEN
        callback.onAuthenticationError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, "Error")

        // THEN
        assertTrue(failureCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun `onAuthenticationError with ERROR_TIMEOUT calls onFailure`() {
        // GIVEN
        var failureCalled = false
        var errorCalled = false
        val callback =
            BiometricAuthHandler.Callback(
                onFailure = { failureCalled = true },
                onError = { _, _ -> errorCalled = true }
            )

        // WHEN
        callback.onAuthenticationError(BiometricPrompt.ERROR_TIMEOUT, "Timeout")

        // THEN
        assertTrue(failureCalled)
        assertFalse(errorCalled)
    }

    @Test
    fun `onAuthenticationError with other error codes calls onError`() {
        // GIVEN
        var errorCode = -1
        var errorMessage = ""
        val callback =
            BiometricAuthHandler.Callback(
                onError = { code, msg ->
                    errorCode = code
                    errorMessage = msg.toString()
                }
            )

        // WHEN
        callback.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "Canceled")

        // THEN
        assertEquals(BiometricPrompt.ERROR_CANCELED, errorCode)
        assertEquals("Canceled", errorMessage)
    }

    @Test
    fun `onAuthenticationSucceeded calls onSuccess`() {
        // GIVEN
        var successCalled = false
        val callback =
            BiometricAuthHandler.Callback(
                onSuccess = { successCalled = true }
            )

        // WHEN
        callback.onAuthenticationSucceeded(mock())

        // THEN
        assertTrue(successCalled)
    }

    @Test
    fun `Callback onAuthenticationFailed calls onFailure`() {
        // GIVEN
        var failureCalled = false
        val callback =
            BiometricAuthHandler.Callback(
                onFailure = { failureCalled = true }
            )

        // WHEN
        callback.onAuthenticationFailed()

        // THEN
        assertTrue(failureCalled)
    }

    @Test
    fun `close sets fragmentActivity to null`() {
        // GIVEN
        val handler = BiometricAuthHandler(activity)
        handler.close()

        // WHEN
        val request =
            BiometricAuthHandler.Request(
                accessControlLevel = AccessControlLevel.PASSCODE,
                promptConfig = BiometricAuthHandler.PromptConfig("Title", "Close"),
                callback = BiometricAuthHandler.Callback()
            )
        handler.authenticate(request)

        // THEN
        // Verify no exception when calling authenticate after close
    }
}
