package uk.gov.android.authentication.integrity.keymanager

/**
 * Exception thrown when a key signing operation fails.
 *
 * This exception wraps underlying cryptographic errors that occur during the signing process,
 * such as key access failures, invalid key states, or signature generation errors.
 *
 * @param alias The key alias that failed to sign the data
 * @param cause The underlying cause of the signing failure
 */
class KeySigningException(
    alias: String,
    cause: Throwable
) : Exception("Failed to sign data with key alias: $alias", cause)
