package uk.gov.android.authentication.integrity

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.interfaces.ECPublicKey
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@ExperimentalEncodingApi
@Suppress("MemberVisibilityCanBePrivate", "unused")
internal class KeystoreManager {
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

    val hasAppCheckKeys: Boolean
        get() = ks.containsAlias(ALIAS)

    val appCheckPrivateKey: PrivateKey
        get() = ks.getKey(ALIAS, null) as PrivateKey

    val appCheckPublicKey: ECPublicKey
        get() = ks.getCertificate(ALIAS).publicKey as ECPublicKey

    init {
        if (!hasAppCheckKeys) {
            Log.d(this::class.simpleName, "Generating key pair")
            createNewKeys()
        }
    }

    fun getPubKeyBase64ECCoord(): Pair<String, String> {
        val xByteArr = appCheckPublicKey.w.affineX.toByteArray()
        val yByteArr = appCheckPublicKey.w.affineY.toByteArray()
        val x = Base64.encode(xByteArr)
        val y = Base64.encode(yByteArr)
        return Pair(x, y)
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

    companion object {
        private const val ALIAS = "app_check_keys"
        private const val KEYSTORE = "AndroidKeyStore"
    }
}
