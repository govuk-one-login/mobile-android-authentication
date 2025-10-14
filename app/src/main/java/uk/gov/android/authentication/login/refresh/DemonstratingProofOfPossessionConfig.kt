package uk.gov.android.authentication.login.refresh

import uk.gov.android.authentication.integrity.pop.ProofOfPossessionGenerator
import uk.gov.android.keystore.KeyStoreManager
import uk.gov.logging.api.Logger

/**
 * Configuration class providing required components for the Demonstrating Proof of Possession Manager
 */
data class DemonstratingProofOfPossessionConfig(
    val logger: Logger,
    val popGenerator: ProofOfPossessionGenerator,
    val keyStoreManager: KeyStoreManager
)
