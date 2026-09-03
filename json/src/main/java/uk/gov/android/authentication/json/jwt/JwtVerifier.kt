package uk.gov.android.authentication.json.jwt

fun interface JwtVerifier {
    fun verify(
        encodedJsonWebToken: String,
        publicKeyJwkString: String,
    ): Boolean
}
