package uk.gov.android.authentication.integrity.appcheck.usecase

import uk.gov.android.authentication.integrity.appcheck.model.AppCheckToken

fun interface AppChecker {
    suspend fun getAppCheckToken(): Result<AppCheckToken>
}
