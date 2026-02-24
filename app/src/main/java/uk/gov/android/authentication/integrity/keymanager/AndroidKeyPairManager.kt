package uk.gov.android.authentication.integrity.keymanager

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.AUTH_BIOMETRIC_STRONG
import android.security.keystore.KeyProperties.AUTH_DEVICE_CREDENTIAL
import android.security.keystore.KeyProperties.KEY_ALGORITHM_EC
import android.security.keystore.KeyProperties.PURPOSE_SIGN
import android.security.keystore.KeyProperties.PURPOSE_VERIFY
import androidx.annotation.VisibleForTesting
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import kotlin.coroutines.suspendCoroutine
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.gov.android.authentication.integrity.AppIntegrityUtils.toFixedLengthBytes
import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler.AccessControlLevel
import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler.Callback
import uk.gov.android.authentication.integrity.keymanager.BiometricAuthHandler.Request
import uk.gov.android.authentication.integrity.keymanager.KeyStoreManager.Companion.convertSignatureToASN1
import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator.getUrlSafeNoPaddingBase64
import uk.gov.logging.api.Logger

@Suppress("TooManyFunctions")
class AndroidKeyPairManager private constructor(
    private val logger: Logger,
    private val userAuthRequired: Boolean,
    private val keyStore: KeyStore,
    private val keyPairGenerator: KeyPairGenerator,
    private val mainDispatcher: CoroutineDispatcher
) : KeyPairManager {
    constructor(
        logger: Logger,
        userAuthRequired: Boolean
    ) : this(
        logger = logger,
        userAuthRequired = userAuthRequired,
        keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) },
        keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM_EC, KEYSTORE),
        mainDispatcher = Dispatchers.Main
    )

    @OptIn(ExperimentalEncodingApi::class)
    override fun getPublicKeyCoordinates(alias: String): Pair<String, String> {
        val publicKey = getPublicKey(alias)
        val xBytes = toFixedLengthBytes(publicKey.w.affineX, EC_POINTS_LENGTH_REQUIREMENT)
        val yBytes = toFixedLengthBytes(publicKey.w.affineY, EC_POINTS_LENGTH_REQUIREMENT)
        val x = getUrlSafeNoPaddingBase64(xBytes)
        val y = getUrlSafeNoPaddingBase64(yBytes)
        return Pair(x, y)
    }

    private fun sign(
        alias: String,
        data: ByteArray
    ): ByteArray =
        runCatching {
            val privateKey = getPrivateKeyEntry(alias).privateKey

            val signature =
                Signature.getInstance(ALG).run {
                    initSign(privateKey)
                    update(data)
                    sign()
                }

            val ecSpec = getPublicKey(alias).params
            convertSignatureToASN1(signature, ecSpec)
        }.getOrElse { exception ->
            logger.nonFatal(exception)
            throw exception
        }

    override suspend fun authenticateAndSign(
        vararg requests: SignRequest,
        promptConfig: BiometricAuthHandler.PromptConfig,
        authHandler: BiometricAuthHandler
    ): List<SignedData> =
        withContext(mainDispatcher) {
            suspendCoroutine { continuation ->
                authHandler.use {
                    it.authenticate(
                        Request(
                            accessControlLevel = AccessControlLevel.PASSCODE_AND_BIOMETRICS,
                            promptConfig = promptConfig,
                            callback =
                            Callback(
                                onSuccess = {
                                    continuation.resumeWith(
                                        runCatching {
                                            requests.toList().map { request ->
                                                SignedData(
                                                    keyAlias = request.keyAlias,
                                                    signature = sign(
                                                        request.keyAlias,
                                                        request.data
                                                    )
                                                )
                                            }
                                        }
                                    )
                                },
                                onError = { code, msg ->
                                    continuation.resumeWith(
                                        Result.failure(
                                            BiometricAuthException(code, msg)
                                        )
                                    )
                                }
                            )
                        )
                    )
                }
            }
        }

    override fun deleteKeyFor(alias: String) {
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
            logger.debug(TAG, "alias: $alias - deleted")
        } else {
            logger.debug(TAG, "alias: $alias - it's not wallet alias")
        }
    }

    override fun deleteAllKeysWithPrefix(prefix: String) {
        val aliases = keyStore.aliases()
        while (aliases.hasMoreElements()) {
            val alias = aliases.nextElement()
            if (alias.startsWith(prefix)) {
                keyStore.deleteEntry(alias)
                logger.debug(TAG, "alias: $alias - deleted")
            } else {
                logger.debug(TAG, "alias: $alias - it's not wallet alias")
            }
        }
    }

    private fun getKeyGenParameterSpec(
        alias: String,
        isStrongBoxBacked: Boolean
    ): KeyGenParameterSpec {
        val spec = KeyGenParameterSpec.Builder(alias, PURPOSE_SIGN or PURPOSE_VERIFY)
        with(spec) {
            setAlgorithmParameterSpec(ECGenParameterSpec(EC_DOMAIN_STANDARD_NAME))
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            setIsStrongBoxBacked(isStrongBoxBacked)

            if (userAuthRequired) {
                setUserAuthenticationRequired(true)
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    setUserAuthenticationValidityDurationSeconds(KEY_TIMEOUT_SECONDS)
                } else {
                    val type = AUTH_DEVICE_CREDENTIAL or AUTH_BIOMETRIC_STRONG
                    setUserAuthenticationParameters(KEY_TIMEOUT_SECONDS, type)
                }
            }
        }
        return spec.build()
    }

    private fun getPrivateKeyEntry(alias: String): KeyStore.PrivateKeyEntry {
        val entry =
            keyStore.getEntry(alias, null) ?: error("Private key not found for alias: $alias")
        return entry as KeyStore.PrivateKeyEntry
    }

    fun getPublicKey(alias: String): ECPublicKey {
        if (!keyStore.containsAlias(alias)) {
            logger.debug(TAG, "alias: $alias - create new POP key")
            createKeyPair(alias)
        }
        logger.debug(TAG, "alias: $alias - get public key")
        return keyStore.getCertificate(alias).publicKey as ECPublicKey
    }

    private fun createKeyPair(alias: String) {
        val isStrongBox = createKeyPair(keyPairGenerator, alias, isStrongBoxBacked = true) != null
        if (!isStrongBox) {
            createKeyPair(keyPairGenerator, alias, isStrongBoxBacked = false)
        }
        val backingType = if (isStrongBox) "StrongBox-backed" else "software-backed"
        logger.debug(TAG, "alias: $alias - KeyPair generated using $backingType KeyStore")
    }

    private fun createKeyPair(
        keyPairGenerator: KeyPairGenerator,
        alias: String,
        isStrongBoxBacked: Boolean
    ): KeyPair? =
        runCatching {
            keyPairGenerator.initialize(getKeyGenParameterSpec(alias, isStrongBoxBacked))
            keyPairGenerator.generateKeyPair()
        }.onFailure { e -> logger.nonFatal(e) }
            .getOrNull()

    /**
     * Logs a non-fatal throwable for both debugging and Crashlytics reporting.
     *
     * In debug builds, logs `Non-fatal` tag and throwable message to Android logs.
     * In all builds, reports [throwable] to Crashlytics.
     *
     * @param throwable The non-fatal throwable to report to Crashlytics.
     */
    private fun Logger.nonFatal(throwable: Throwable) {
        error(TAG, throwable.message.toString(), throwable)
    }

    companion object {
        /**
         * Authentication validity timeout in seconds for KeyStore keys.
         *
         * This value determines how long a key remains unlocked after successful biometric/device
         * credential authentication, allowing multiple cryptographic operations within the timeout window.
         *
         * **Note:** Keys created with stricter authentication requirements (e.g., per-operation with timeout=0)
         * can be used with less strict requirements (e.g., timed window). However, keys created with a
         * specific timeout cannot be made MORE restrictive without recreation.
         *
         * Current value of 15 seconds allows multiple signature operations with a single authentication prompt.
         */
        @VisibleForTesting
        const val KEY_TIMEOUT_SECONDS = 15
        private const val ALG = "SHA256withECDSA"
        const val TAG = "AndroidKeyPairManager"
        const val KEYSTORE = "AndroidKeyStore"
        private const val EC_DOMAIN_STANDARD_NAME = "secp256r1"
        private const val EC_POINTS_LENGTH_REQUIREMENT = 32

        @VisibleForTesting
        fun createForTesting(
            logger: Logger,
            userAuthRequired: Boolean,
            keyStore: KeyStore,
            keyPairGenerator: KeyPairGenerator,
            mainDispatcher: CoroutineDispatcher
        ): AndroidKeyPairManager =
            AndroidKeyPairManager(
                logger,
                userAuthRequired,
                keyStore,
                keyPairGenerator,
                mainDispatcher
            )
    }
}
