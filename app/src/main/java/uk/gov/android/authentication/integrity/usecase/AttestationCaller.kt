package uk.gov.android.authentication.integrity.usecase

@Suppress("unused")
fun interface AttestationCaller {
    suspend fun call(
        signedProofOfPossession: String,
        jwkX: String,
        jwkY: String
    ): Result<Response>

    data class Response(val jwt: String, val expiresIn: Long)

    companion object {
        protected const val FIREBASE_HEADER = "X-Firebase-AppCheck"
    }
}
