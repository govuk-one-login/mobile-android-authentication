package uk.gov.android.authentication.integrity.appcheck.model

import kotlinx.serialization.Serializable

@Serializable
data class AppCheckToken(
    val jwtToken: String
)
