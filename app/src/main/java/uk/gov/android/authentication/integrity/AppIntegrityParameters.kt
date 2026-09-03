package uk.gov.android.authentication.integrity

import kotlinx.serialization.Serializable

@Serializable
data class AppIntegrityParameters(
    val attestation: String,
    val pop: String,
)
