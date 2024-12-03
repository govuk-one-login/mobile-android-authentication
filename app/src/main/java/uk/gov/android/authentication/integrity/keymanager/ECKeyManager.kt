package uk.gov.android.authentication.integrity.keymanager

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.google.gson.JsonParseException
import uk.gov.android.authentication.integrity.AppIntegrityUtils
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import uk.gov.android.authentication.jwt.Jose4jJwtVerifier
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.PrivateKeyEntry
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
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

    override fun getPublicKey(): Pair<String, String> {
        val xByteArr = appCheckPublicKey.w.affineX
        val yByteArr = appCheckPublicKey.w.affineY
        val xCheckedArray = AppIntegrityUtils
            .toFixedLengthBytes(xByteArr, EC_POINTS_LENGTH_REQUIREMENT)
        val yCheckedArray = AppIntegrityUtils
            .toFixedLengthBytes(yByteArr, EC_POINTS_LENGTH_REQUIREMENT)
        val x = ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(xCheckedArray)
        val y = ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64(yCheckedArray)
        return Pair(x, y)
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


    override fun verify(jwt: String, jwk: String): Boolean {
        return try {
            Jose4jJwtVerifier().verify(jwk, jwk)
        } catch (e: JsonParseException) {
            Log.e(this::class.simpleName, e.toString())
            throw SigningError.InvalidSignature
        } catch (e: IllegalArgumentException) {
            Log.e(this::class.simpleName, e.toString())
            throw SigningError.InvalidSignature
        }
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

    sealed class SigningError(error: String) : Exception(error) {
        data object InvalidSignature : SigningError("Signature couldn't be verified.")
    }

    companion object {
        private const val ALIAS = "app_check_keys"
        private const val KEYSTORE = "AndroidKeyStore"
        private const val ALG = "SHA256withECDSA"
        private const val EC_POINTS_LENGTH_REQUIREMENT = 32
    }
}
