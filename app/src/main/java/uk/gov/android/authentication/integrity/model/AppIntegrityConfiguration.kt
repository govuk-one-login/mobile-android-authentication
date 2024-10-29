package uk.gov.android.authentication.integrity.model

import uk.gov.android.authentication.integrity.appcheck.AppChecker
import uk.gov.android.authentication.integrity.usecase.AttestationClient

data class AppIntegrityConfiguration(
    val attestationClient: AttestationClient,
    val appChecker: AppChecker
)
