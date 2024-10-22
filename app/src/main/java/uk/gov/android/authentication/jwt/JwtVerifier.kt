package uk.gov.android.authentication.jwt

import androidx.annotation.VisibleForTesting

fun interface JwtVerifier {
    fun verify(encodedJsonWebToken: String, publicKeyJwkString: String): Boolean

    sealed class VerifyJwtException(msg: String) : Exception(msg)

    companion object {
        @JvmStatic
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun stub(isVerified: Boolean) = JwtVerifier { _, _ -> isVerified }

        @JvmStatic
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun throwable(throwable: Throwable) = JwtVerifier { _, _ -> throw throwable }
    }
}
