package uk.gov.android.authentication.integrity.usecase

import org.jose4j.jwk.JsonWebKey

@Suppress("MemberVisibilityCanBePrivate")
object JWK {
    const val keyType = "kty"
    const val use = "use"
    const val curve = "crv"
    const val x = "x"
    const val y = "y"
    private const val keyTypeValue = "EC"
    private const val useValue = "sig"
    private const val curveValue = "P-256"

    fun makeJWK(x: String, y: String): JsonWebKey = JsonWebKey.Factory.newJwk(
        mapOf(
            keyType to keyTypeValue,
            use to useValue,
            curve to curveValue,
            JWK.x to x,
            JWK.y to y
        )
    )
}
