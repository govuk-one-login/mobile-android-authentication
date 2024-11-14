package uk.gov.android.authentication.integrity.model

import uk.gov.android.authentication.integrity.appcheck.usecase.AppChecker
import uk.gov.android.authentication.integrity.keymanager.KeyStoreManager
import uk.gov.android.authentication.integrity.appcheck.usecase.AttestationCaller

data class AppIntegrityConfiguration(
    val attestationCaller: AttestationCaller,
    val appChecker: AppChecker,
    val keyStoreManager: KeyStoreManager
)
