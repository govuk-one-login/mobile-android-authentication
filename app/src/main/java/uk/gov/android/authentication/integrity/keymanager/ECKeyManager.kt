package uk.gov.android.authentication.integrity.keymanager

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.Signature
import java.security.interfaces.ECPublicKey
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
@Suppress("MemberVisibilityCanBePrivate", "unused")
class ECKeyManager : KeyStoreManager {
    private val ks: KeyStore = KeyStore.getInstance(KEYSTORE).apply {
        load(null)
    }

    private val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
        ALIAS,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    ).run {
        setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
        build()
    }

    private val hasAppCheckKeys: Boolean
        get() = ks.containsAlias(ALIAS)

    private val appCheckPrivateKeyEntry: PrivateKeyEntry
        get() = ks.getEntry(ALIAS, null) as PrivateKeyEntry

    private val appCheckPublicKey: ECPublicKey
        get() = appCheckPrivateKeyEntry.certificate.publicKey as ECPublicKey

    init {
        if (!hasAppCheckKeys) {
            Log.d(this::class.simpleName, "Generating key pair")
            createNewKeys()
        }
    }

    override fun getPublicKey(): Pair<String, String> {
        val xByteArr = appCheckPublicKey.w.affineX.toByteArray()
        val yByteArr = appCheckPublicKey.w.affineY.toByteArray()
        val x = ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(xByteArr)
        val y = ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(yByteArr)
        return Pair(x, y)
    }

    override fun sign(input: ByteArray): ByteArray {
        val signature = Signature.getInstance(ALG).run {
            initSign(appCheckPrivateKeyEntry.privateKey)
            update(input)
            sign()
        }
        val verifyResult = verify(input, signature)
        Log.d("VerifySignature", "$verifyResult")
        return signature
    }


    override fun verify(data: ByteArray, signature: ByteArray): Boolean {
        val successfulSignature = Signature.getInstance(ALG).run {
            initVerify(appCheckPrivateKeyEntry.certificate)
            update(data)
            verify(signature)
        }
        if (!successfulSignature) throw SigningError.InvalidSignature
        return true
    }

    private fun createNewKeys() {
        KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            KEYSTORE
        ).apply {
            initialize(parameterSpec)
            genKeyPair()
        }
    }

    sealed class SigningError(error: String) : Exception(error) {
        data object InvalidSignature : SigningError("Signature couldn't be verified.")
    }

    companion object {
        private const val ALIAS = "app_check_keys"
        private const val KEYSTORE = "AndroidKeyStore"
        private const val ALG = "SHA256withECDSA"
    }
}
