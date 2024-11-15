package uk.gov.android.authentication.integrity.appcheck.usecase

import uk.gov.android.authentication.integrity.appcheck.model.AttestationResponse

@Suppress("unused")
fun interface AttestationCaller {
    suspend fun call(
        firebaseToken: String,
        jwk: JWK.JsonWebKey
    ): AttestationResponse

    companion object {
        const val FIREBASE_HEADER = "X-Firebase-AppCheck"
        const val CONTENT_TYPE = "Content-type"
        const val CONTENT_TYPE_VALUE = "application/json"
    }
}
