package uk.gov.android.authentication.integrity.appcheck

import android.content.Context
import uk.gov.android.authentication.integrity.model.AppCheckToken

interface AppChecker {
    fun init(context: Context)

    suspend fun getAppCheckToken(): Result<AppCheckToken>
}
