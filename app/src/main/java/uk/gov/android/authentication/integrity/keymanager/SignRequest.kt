package uk.gov.android.authentication.integrity.keymanager

/**
 * Represents a request to sign data with a specific key.
 *
 * @property keyAlias The alias of the key in the KeyStore to use for signing
 * @property data The data to be signed
 */
data class SignRequest(
    val keyAlias: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignRequest) return false
        return keyAlias == other.keyAlias && data.contentEquals(other.data)
    }

    override fun hashCode(): Int {
        var result = keyAlias.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
