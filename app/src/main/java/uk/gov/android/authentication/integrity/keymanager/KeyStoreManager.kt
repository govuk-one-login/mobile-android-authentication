package uk.gov.android.authentication.integrity.keymanager

interface KeyStoreManager {
    fun getPublicKey(): Pair<String, String>
    fun sign(input: ByteArray): ByteArray
    fun verify(data: ByteArray, signature: ByteArray): Boolean
}
