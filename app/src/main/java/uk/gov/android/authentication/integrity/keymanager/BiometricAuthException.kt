package uk.gov.android.authentication.integrity.keymanager

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
