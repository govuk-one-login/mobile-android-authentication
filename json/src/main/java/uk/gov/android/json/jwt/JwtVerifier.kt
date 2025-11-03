package uk.gov.android.json.jwt

fun interface JwtVerifier {
    fun verify(encodedJsonWebToken: String, publicKeyJwkString: String): Boolean
}
