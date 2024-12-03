package uk.gov.android.authentication.integrity.keymanager

import org.bouncycastle.asn1.ASN1InputStream
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import uk.gov.android.authentication.integrity.AppIntegrityUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.spec.ECParameterSpec

interface KeyStoreManager {
    fun getPublicKey(): Pair<String, String>
    fun sign(input: ByteArray): ByteArray
    fun verify(jwt: String, jwk: String): Boolean

    companion object {
        fun convertSignatureToASN1(signature: ByteArray, spec: ECParameterSpec): ByteArray {
            // Convert signature to bites as values
            val asn1Stream = ASN1InputStream(ByteArrayInputStream(signature))
            val sequence = asn1Stream.readObject() as ASN1Sequence
            val r = (sequence.getObjectAt(0) as ASN1Integer).value
            val s = (sequence.getObjectAt(1) as ASN1Integer).value
            // Get the required length for the keys r and s
            val keySizeBytes = spec.order.bitLength() / 8
            // Generate ByteArrays in correct format for r and s
            val rBytes = AppIntegrityUtils.toFixedLengthBytes(r, keySizeBytes)
            val sBytes = AppIntegrityUtils.toFixedLengthBytes(s, keySizeBytes)
            // Consolidate the two values into a ByteArray
            val outputStream = ByteArrayOutputStream()
            outputStream.write(rBytes)
            outputStream.write(sBytes)
            return outputStream.toByteArray()
        }
    }
}
