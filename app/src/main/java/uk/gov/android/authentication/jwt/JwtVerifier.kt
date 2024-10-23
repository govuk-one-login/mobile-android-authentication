package uk.gov.android.authentication.jwt

fun interface JwtVerifier {
    fun verify(encodedJsonWebToken: String, publicKeyJwkString: String): Boolean
}
