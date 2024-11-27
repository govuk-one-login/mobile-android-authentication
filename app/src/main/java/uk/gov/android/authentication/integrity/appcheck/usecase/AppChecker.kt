package uk.gov.android.authentication.integrity.appcheck.usecase

import uk.gov.android.authentication.integrity.appcheck.model.AppCheckToken

/**
 * Interface to perform a network request that provides an [AppCheckToken] from a provider
 * (e.g. FirebaseAppCheckFactory, custom backend AppCheck provider, etc).
 *
 * It is used in the process of providing a ClientAttestation for the AppIntegrityCheck - allows
 * the integrity package to be encapsulated and not have any dependencies on network packages.
 */
fun interface AppChecker {
    /**
     * Retrieves a token that will be used in verifying the authenticity of the app.
     *
     * @return An [AppCheckToken] when successful, containing a JWT
     *         or a [Result] containing the failure, otherwise.
     */
    suspend fun getAppCheckToken(): Result<AppCheckToken>
}
