package uk.gov.android.authentication.integrity.model

import uk.gov.android.authentication.integrity.appcheck.AppChecker
import uk.gov.android.network.client.GenericHttpClient

data class AppIntegrityConfiguration(
    val httpClient: GenericHttpClient,
    val attestationUrl: String,
    val appChecker: AppChecker
)
