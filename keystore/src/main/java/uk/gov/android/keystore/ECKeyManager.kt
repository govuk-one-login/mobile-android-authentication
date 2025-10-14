package uk.gov.android.keystore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import kotlin.io.encoding.ExperimentalEncodingApi
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator

@ExperimentalEncodingApi
@Suppress("MemberVisibilityCanBePrivate")
class ECKeyManager : KeyStoreManager {
    private val ks: KeyStore = KeyStore.getInstance(KEYSTORE).apply {
        load(null)
    }

    private val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
        ALIAS,
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
    ).run {
        setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
        setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
        setUserAuthenticationRequired(false)
        build()
    }

    private val hasAppCheckKeys: Boolean
        get() = ks.containsAlias(ALIAS)

    private val appCheckPrivateKeyEntry: PrivateKeyEntry
        get() = ks.getEntry(ALIAS, null) as PrivateKeyEntry

    private val appCheckPublicKey: ECPublicKey
        get() = ks.getCertificate(ALIAS).publicKey as ECPublicKey

    init {
        if (!hasAppCheckKeys) {
            Log.d(this::class.simpleName, "Generating key pair")
            createNewKeys()
        }
    }

    override fun getPublicKeyCoordinates(): Pair<String, String> {
        val xByteArr = appCheckPublicKey.w.affineX
        val yByteArr = appCheckPublicKey.w.affineY
        val xCheckedArray = Utils
            .toFixedLengthBytes(xByteArr, EC_POINTS_LENGTH_REQUIREMENT)
        val yCheckedArray = Utils
            .toFixedLengthBytes(yByteArr, EC_POINTS_LENGTH_REQUIREMENT)
        val x = ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(xCheckedArray)
        val y = ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(yCheckedArray)
        return Pair(x, y)
    }

    override fun getPublicKey(): ECPublicKey {
        return appCheckPublicKey
    }

    override fun sign(input: ByteArray): ByteArray {
        val ecSpec = appCheckPublicKey.params
        val signature = Signature.getInstance(ALG).run {
            initSign(appCheckPrivateKeyEntry.privateKey)
            update(input)
            sign()
        }

        return KeyStoreManager.convertSignatureToASN1(signature, ecSpec)
    }

    private fun createNewKeys() {
        KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            KEYSTORE
        ).apply {
            initialize(parameterSpec)
            generateKeyPair()
        }
    }

    companion object {
        private const val ALIAS = "app_check_keys"
        private const val KEYSTORE = "AndroidKeyStore"
        private const val ALG = "SHA256withECDSA"
        private const val EC_POINTS_LENGTH_REQUIREMENT = 32
    }
}
