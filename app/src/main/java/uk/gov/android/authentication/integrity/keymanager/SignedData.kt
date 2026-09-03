package uk.gov.android.authentication.integrity.keymanager

/**
 * Represents signed data with its associated key alias.
 *
 * @property keyAlias The alias of the key used for signing
 * @property signature The cryptographic signature in ASN.1 format
 */
data class SignedData(
    val keyAlias: String,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignedData) return false
        return keyAlias == other.keyAlias && signature.contentEquals(other.signature)
    }

    override fun hashCode(): Int {
        var result = keyAlias.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}
