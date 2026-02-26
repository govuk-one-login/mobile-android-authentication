package uk.gov.android.authentication.integrity.keymanager

import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler.PromptConfig

interface KeyPairManager {
    /**
     * Retrieves the public key coordinates (x, y) for the key identified by [alias].
     *
     * @param alias The key alias in the KeyStore
     * @return Pair of base64-encoded x and y coordinates
     */
    fun getPublicKeyCoordinates(alias: String): Pair<String, String>

    /**
     * Authenticates the user and signs multiple data items with their respective keys.
     *
     * This method prompts for biometric or device credential authentication once, then uses the
     * authenticated keys to sign all provided data. Keys remain unlocked for a short period
     * after successful authentication, allowing multiple signature operations without re-authentication.
     *
     * @param requests Sign requests containing key aliases and data to sign
     * @param promptConfig Configuration for the authentication prompt (title, subtitle, description)
     * @param authHandler Handler for biometric authentication UI
     * @return List of signed data with signatures in the same order as requests
     * @throws Exception if authentication fails or any signing operation fails
     */
    suspend fun authenticateAndSign(
        vararg requests: SignRequest,
        promptConfig: PromptConfig,
        authHandler: BiometricAuthHandler
    ): List<SignedData>

    /**
     * Deletes the key identified by [alias] from the KeyStore.
     *
     * @param alias The key alias to delete
     */
    fun deleteKeyFor(alias: String)

    /**
     * Deletes all keys with the specified [prefix] from the KeyStore.
     *
     * @param prefix The prefix to match against key aliases
     */
    fun deleteAllKeysWithPrefix(prefix: String)
}
