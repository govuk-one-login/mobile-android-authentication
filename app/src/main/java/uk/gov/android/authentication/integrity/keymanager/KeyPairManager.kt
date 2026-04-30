package uk.gov.android.authentication.integrity.keymanager

import java.io.ByteArrayInputStream
import java.security.interfaces.ECPublicKey
import java.security.spec.ECParameterSpec
import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import uk.gov.android.authentication.integrity.AppIntegrityUtils
import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler.PromptConfig

interface KeyPairManager {
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
     * @throws BiometricAuthException if authentication fails or any signing operation fails
     */
    suspend fun authenticateAndSign(
        vararg requests: SignRequest,
        promptConfig: PromptConfig,
        authHandler: BiometricAuthHandler
    ): List<SignedData>

    /**
     * Deletes all keys with the specified [prefix] from the KeyStore.
     *
     * @param prefix The prefix to match against key aliases
     */
    fun deleteAllKeysWithPrefix(prefix: String)

    /**
     * Deletes the key identified by [alias] from the KeyStore.
     *
     * @param alias The key alias to delete
     */
    fun deleteKeyFor(alias: String)

    /**
     * Retrieves the EC public key for the key identified by [alias].
     *
     * If the key doesn't exist in the KeyStore, it will be created automatically.
     *
     * @param alias The key alias in the KeyStore
     * @return The EC public key associated with the alias
     */

    fun getPublicKey(alias: String): ECPublicKey

    /**
     * Retrieves the public key coordinates (x, y) for the key identified by [alias].
     *
     * @param alias The key alias in the KeyStore
     * @return Pair of base64-encoded x and y coordinates
     */
    fun getPublicKeyCoordinates(alias: String): Pair<String, String>

    /**
     * Signs the provided data using the private key identified by [alias].
     *
     * The signature is converted to ASN.1 DER format for compatibility with JWT and other
     * cryptographic standards. If the key doesn't exist, it will be created automatically.
     *
     * @param alias The key alias in the KeyStore
     * @param data The data to sign
     * @return The signature in ASN.1 DER format as a byte array
     * @throws KeySigningException if signing fails or the key cannot be accessed
     */
    fun sign(
        alias: String,
        data: ByteArray
    ): ByteArray

    companion object {
        /**
         * Converts an ECDSA signature from ASN.1 DER format to raw concatenated format.
         *
         * ECDSA signatures are typically generated in ASN.1 DER format (SEQUENCE of two INTEGERs: r and s).
         * This method extracts the r and s values, converts them to fixed-length byte arrays based on
         * the curve's order, and concatenates them into a single byte array (r || s).
         *
         * This format is required for JWT signatures and other cryptographic protocols that expect
         * raw ECDSA signatures rather than ASN.1 encoded ones.
         *
         * @param signature The ECDSA signature in ASN.1 DER format
         * @param spec The EC parameter specification containing the curve order
         * @return The signature as concatenated r and s values in fixed-length byte arrays
         */
        fun convertSignatureToASN1(signature: ByteArray, spec: ECParameterSpec): ByteArray {
            // Convert signature to bites as values
            val asn1Stream = ASN1InputStream(ByteArrayInputStream(signature))
            val sequence = asn1Stream.readObject() as ASN1Sequence
            val r = (sequence.getObjectAt(0) as ASN1Integer).value
            val s = (sequence.getObjectAt(1) as ASN1Integer).value
            // Get the required length for the keys r and s
            val keySizeBytes = spec.order.bitLength() / TO_BYTE
            // Generate ByteArrays in correct format for r and s
            val rBytes = AppIntegrityUtils.toFixedLengthBytes(r, keySizeBytes)
            val sBytes = AppIntegrityUtils.toFixedLengthBytes(s, keySizeBytes)
            // Consolidate the two values into a ByteArray
            return rBytes + sBytes
        }

        private const val TO_BYTE = 8
    }
}
