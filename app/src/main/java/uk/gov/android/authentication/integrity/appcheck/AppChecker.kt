package uk.gov.android.authentication.integrity.appcheck

import uk.gov.android.authentication.integrity.model.AppCheckToken

interface AppChecker {
    suspend fun getAppCheckToken(): Result<AppCheckToken>
}
