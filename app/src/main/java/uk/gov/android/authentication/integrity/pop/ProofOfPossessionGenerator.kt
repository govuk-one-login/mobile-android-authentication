package uk.gov.android.authentication.integrity.pop

import java.time.Instant
import kotlin.io.encoding.Base64
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import uk.gov.android.authentication.json.jwk.JWK

/**
 * Generator for Proof of Possession (PoP) tokens used in OAuth 2.0 authentication flows.
 *
 * This object provides factory methods to create unsigned JWT tokens that demonstrate possession
 * of cryptographic keys or prove the authenticity of the client application. These tokens are
 * used in various authentication scenarios:
 *
 * - **Client Attestation PoP**: Validates app integrity during initial authentication
 * - **DPoP (Demonstrating Proof-of-Possession)**: Secures token refresh operations
 * - **OpenID4VCI PoP**: Proves key possession for verifiable credential issuance
 *
 * ## Token Structure
 *
 * All generated tokens follow the JWT structure (header.payload) but are **unsigned**.
 * The tokens are Base64URL-encoded without padding, as per RFC 7515.
 *
 * Example output format:
 * ```
 * eyJhbGciOiJFUzI1NiIsInR5cCI6Im9hdXRoLWNsaWVudC1hdHRlc3RhdGlvbi1wb3Arand0In0.eyJpc3MiOiJ1cm46ZmRjOmdvdjp1azp3YWxsZXQiLCJhdWQiOiJodHRwczovL3Rva2VuLmFjY291bnQuZ292LnVrIiwiZXhwIjoxNzM0NTY3ODkwLCJqdGkiOiJkM2U4ZTM4Mi00NjkxLTRiMWYtODNjMS00NDU0Zjc1YmQ5MzAifQ
 * ```
 *
 * ## Security Considerations
 *
 * - Tokens are **not signed** by this generator; signing must be performed separately
 * - All timestamps are in Unix epoch seconds
 * - JTI (JWT ID) values are randomly generated UUIDs to prevent replay attacks
 * - Tokens should be transmitted over secure channels (HTTPS) only
 *
 * @see [RFC 7515 - JSON Web Signature (JWS)](https://tools.ietf.org/html/rfc7515)
 * @see [RFC 9449 - OAuth 2.0 Demonstrating Proof-of-Possession](https://tools.ietf.org/html/rfc9449)
 */
object ProofOfPossessionGenerator {
    /**
     * Creates a client attestation Proof of Possession token for app integrity verification.
     *
     * This token is used during the initial authentication flow to prove the client application's
     * authenticity. It must be signed separately before being sent to the authorization server.
     *
     * **Header:**
     * ```json
     * {
     *   "alg": "ES256",
     *   "typ": "oauth-client-attestation-pop+jwt"
     * }
     * ```
     *
     * **Payload:**
     * ```json
     * {
     *   "iss": "<OAuth client ID>",
     *   "aud": "<Token endpoint URL>",
     *   "exp": <expiration timestamp>,
     *   "jti": "<unique identifier>"
     * }
     * ```
     *
     * @param iss Issuer - typically the OAuth client ID
     * @param aud Audience - the token endpoint URL that will validate this PoP
     * @param exp Expiration time in Unix epoch seconds
     * @param jti JWT ID - unique identifier to prevent replay attacks (auto-generated if not provided)
     * @return Base64URL-encoded JWT string (header.payload) without signature
     */
    @OptIn(ExperimentalUuidApi::class)
    fun createBase64PoP(
        iss: String,
        aud: String,
        exp: Long,
        jti: String = Uuid.random().toString()
    ): String {
        val headerJson = buildJsonObject {
            put("alg", ALG)
            put("typ", APP_INTEGRITY_TYP)
        }

        val payloadJson = buildJsonObject {
            put("iss", iss)
            put("aud", aud)
            put("exp", exp)
            put("jti", jti)
        }

        return encodeJwtParts(headerJson, payloadJson)
    }

    /**
     * Creates a DPoP (Demonstrating Proof-of-Possession) token for secure token refresh operations.
     *
     * DPoP tokens bind access tokens to a specific client by proving possession of a private key.
     * This token includes the public key (JWK) in the header and must be signed with the
     * corresponding private key.
     *
     * **Header:**
     * ```json
     * {
     *   "alg": "ES256",
     *   "typ": "dpop+jwt",
     *   "jwk": {
     *     "kty": "EC",
     *     "use": "sig",
     *     "crv": "P-256",
     *     "x": "<base64url-encoded x coordinate>",
     *     "y": "<base64url-encoded y coordinate>"
     *   }
     * }
     * ```
     *
     * **Payload:**
     * ```json
     * {
     *   "jti": "<unique identifier>",
     *   "htm": "POST",
     *   "htu": "<token endpoint URL>",
     *   "iat": <issued at timestamp>
     * }
     * ```
     *
     * @param jwk JSON Web Key containing the public key to be included in the header
     * @param htu HTTP URI - the token endpoint URL where this DPoP will be used
     * @param jti JWT ID - unique identifier (auto-generated if not provided)
     * @param iat Issued at time in Unix epoch seconds (defaults to current time)
     * @return Base64URL-encoded JWT string (header.payload) without signature
     * @see [RFC 9449 - OAuth 2.0 Demonstrating Proof-of-Possession](https://tools.ietf.org/html/rfc9449)
     */
    @OptIn(ExperimentalUuidApi::class)
    fun createBase64DPoP(
        jwk: JWK.JsonWebKey,
        htu: String,
        jti: String = Uuid.random().toString(),
        iat: Long = getIssueTime()
    ): String {
        val headerJson = buildJsonObject {
            put("alg", ALG)
            put("typ", REFRESH_TYP)
            putJsonObject("jwk") {
                put("kty", jwk.jwk.kty)
                put("use", jwk.jwk.use)
                put("crv", jwk.jwk.crv)
                put("x", jwk.jwk.x)
                put("y", jwk.jwk.y)
            }
        }

        val payloadJson = buildJsonObject {
            put("jti", jti)
            put("htm", HTTP_METHOD)
            put("htu", htu)
            put("iat", iat)
        }

        return encodeJwtParts(headerJson, payloadJson)
    }

    /**
     * Creates a Proof of Possession token for OpenID4VCI (Verifiable Credential Issuance).
     *
     * This token proves possession of a cryptographic key identified by a DID (Decentralized
     * Identifier) during the credential issuance flow. The token includes a server-provided
     * nonce to prevent replay attacks.
     *
     * **Header:**
     * ```json
     * {
     *   "alg": "ES256",
     *   "typ": "openid4vci-proof+jwt",
     *   "kid": "<DID key identifier>"
     * }
     * ```
     *
     * **Payload:**
     * ```json
     * {
     *   "iss": "<client identifier>",
     *   "iat": <issued at timestamp>,
     *   "nonce": "<server-provided nonce>",
     *   "aud": "<credential issuer URL>"
     * }
     * ```
     *
     * @param kid Key ID - DID key identifier (e.g., "did:key:z6MkTest...")
     * @param nonce Server-provided nonce value to prevent replay attacks
     * @param aud Audience - the credential issuer URL
     * @param iss Issuer - client identifier (e.g., "urn:fdc:gov:uk:wallet")
     * @return Base64URL-encoded JWT string (header.payload) without signature
     * @see [OpenID4VCI Specification](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html)
     */
    fun createBase64DidKeyPoP(
        kid: String,
        nonce: String,
        aud: String,
        iss: String
    ): String {
        val headerJson = buildJsonObject {
            put("alg", ALG)
            put("typ", OPENID4VCI_TYP)
            put("kid", kid)
        }

        val payloadJson = buildJsonObject {
            put("iss", iss)
            put("iat", getIssueTime())
            put("nonce", nonce)
            put("aud", aud)
        }

        return encodeJwtParts(headerJson, payloadJson)
    }

    /**
     * Encodes JWT header and payload into Base64URL format.
     *
     * Converts JSON objects to Base64URL-encoded strings without padding and joins them
     * with a period separator to form an unsigned JWT token.
     *
     * @param header JWT header as a JSON object
     * @param payload JWT payload as a JSON object
     * @return Base64URL-encoded string in format "header.payload"
     */
    private fun encodeJwtParts(
        header: kotlinx.serialization.json.JsonObject,
        payload: kotlinx.serialization.json.JsonObject
    ): String {
        val headerBase64 = getUrlSafeNoPaddingBase64(header.toString().toByteArray())
        val payloadBase64 = getUrlSafeNoPaddingBase64(payload.toString().toByteArray())
        return "$headerBase64.$payloadBase64"
    }

    /**
     * Calculates the expiration timestamp for a token.
     *
     * @return Unix epoch timestamp in seconds, 3 minutes from the current time
     */
    fun getExpiryTime(): Long {
        val minuteUntilExpiry = (MINUTES * MINUTE_IN_MILLISECONDS)
        val expiry = (Instant.now().toEpochMilli() + minuteUntilExpiry) / CONVERT_TO_SECONDS
        return expiry
    }

    /**
     * Gets the current timestamp for token issuance.
     *
     * @return Current Unix epoch timestamp in seconds
     */
    fun getIssueTime(): Long {
        return Instant.now().toEpochMilli() / CONVERT_TO_SECONDS
    }

    /**
     * Encodes a byte array to Base64URL format without padding.
     *
     * Uses URL-safe Base64 encoding (RFC 4648) which replaces '+' with '-'
     * and '/' with '_', and omits padding characters ('=').
     *
     * @param input Byte array to encode
     * @return Base64URL-encoded string without padding
     */
    fun getUrlSafeNoPaddingBase64(input: ByteArray): String {
        return Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
            .encode(input)
    }

    /**
     * Checks if a Proof of Possession token has expired.
     *
     * @param exp Expiration timestamp in Unix epoch seconds
     * @return `true` if the token has expired or is expiring now, `false` otherwise
     */
    fun isPopExpired(exp: Long): Boolean {
        return exp <= (Instant.now().toEpochMilli() / CONVERT_TO_SECONDS)
    }

    private const val ALG = "ES256"
    private const val APP_INTEGRITY_TYP = "oauth-client-attestation-pop+jwt"
    private const val OPENID4VCI_TYP = "openid4vci-proof+jwt"
    private const val REFRESH_TYP = "dpop+jwt"
    private const val HTTP_METHOD = "POST"
    private const val MINUTES = 3
    private const val MINUTE_IN_MILLISECONDS = 60000
    private const val CONVERT_TO_SECONDS = 1000
}
