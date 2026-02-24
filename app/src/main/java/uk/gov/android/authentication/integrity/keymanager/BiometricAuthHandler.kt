package uk.gov.android.authentication.integrity.keymanager

import android.os.Build
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

class BiometricAuthHandler(
    activity: FragmentActivity
) : AutoCloseable {
    private var fragmentActivity: FragmentActivity? = null

    init {
        fragmentActivity = activity
    }

    fun authenticate(request: Request) =
        with(request) {
            require(accessControlLevel != AccessControlLevel.OPEN)

            val promptInfoBuilder =
                BiometricPrompt.PromptInfo
                    .Builder()
                    .setTitle(promptConfig.title)
                    .setSubtitle(promptConfig.subTitle)
                    .setDescription(promptConfig.description)
                    .setConfirmationRequired(false)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                promptInfoBuilder
                    .setNegativeButtonText(promptConfig.negativeButton)
                    .setAllowedAuthenticators(BIOMETRIC_STRONG)
                    .setDeviceCredentialAllowed(true)
            } else {
                promptInfoBuilder.setAllowedAuthenticators(accessControlLevel.toAuthenticators())
            }

            fragmentActivity?.let {
                @Suppress("kotlin:S6293")
                BiometricPrompt(it, callback).authenticate(promptInfoBuilder.build())
            }
        }

    override fun close() {
        fragmentActivity = null
    }

    data class Request(
        val accessControlLevel: AccessControlLevel,
        val promptConfig: PromptConfig,
        val callback: Callback
    )

    data class PromptConfig(
        val title: String,
        val negativeButton: String,
        val subTitle: String? = null,
        val description: String? = null
    )

    enum class AccessControlLevel {
        OPEN,
        PASSCODE,
        PASSCODE_AND_BIOMETRICS
        ;

        fun toAuthenticators(): Int =
            when (this) {
                OPEN -> -1
                PASSCODE -> DEVICE_CREDENTIAL
                PASSCODE_AND_BIOMETRICS -> BIOMETRIC_STRONG or DEVICE_CREDENTIAL
            }
    }

    class Callback(
        val onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit = {},
        val onError: (
            errorCode: Int,
            errString: CharSequence
        ) -> Unit = { _, _ -> },
        val onFailure: () -> Unit = {}
    ) : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(
            errorCode: Int,
            errString: CharSequence
        ) {
            super.onAuthenticationError(errorCode, errString)
            when (errorCode) {
                // Face Scan operated on a one try flow basis which means that it registers the first scan and call onError() if
                // face is not recognised, instead of onFailure. This check below allows for FaceScan to have the same behaviour as
                // Fingerprint allowing multiple attempts with FaceScan
                BiometricPrompt.ERROR_UNABLE_TO_PROCESS,
                BiometricPrompt.ERROR_TIMEOUT -> onFailure()
                else -> onError(errorCode, errString)
            }
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onSuccess(result)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            onFailure()
        }
    }
}

/**
 * Exception thrown when biometric authentication fails.
 *
 * @param code The error code from the biometric authentication system
 * @param message The error message from the biometric authentication system
 */
class BiometricAuthException(
    code: Int,
    message: CharSequence
) : Exception("Biometric authentication failed: $code - $message")
