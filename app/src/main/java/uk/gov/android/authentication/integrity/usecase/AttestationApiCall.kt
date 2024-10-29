package uk.gov.android.authentication.integrity.usecase

internal fun interface AttestationApiCaller {
    suspend fun call(
        signedProofOfPossession: String,
        jwkX: String,
        jwkY: String
    ): String
}

internal class AttestationApiCallerImpl (
    private val client: AttestationClient
) : AttestationApiCaller {

    override suspend fun call(
        signedProofOfPossession: String,
        jwkX: String,
        jwkY: String
    ): String {
        val result = client.attest(signedProofOfPossession, JWK.makeJWK(jwkX, jwkY))
        return when {
            result.isSuccess -> result.getOrNull()?.jwt ?: "Empty"
            else -> result.exceptionOrNull()?.message ?: "Error"
        }
    }
}
