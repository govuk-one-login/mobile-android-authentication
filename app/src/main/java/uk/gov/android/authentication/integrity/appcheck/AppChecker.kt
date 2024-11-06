package uk.gov.android.authentication.integrity.appcheck

import uk.gov.android.authentication.integrity.model.AppCheckToken

fun interface AppChecker {
    suspend fun getAppCheckToken(): Result<AppCheckToken>
}
