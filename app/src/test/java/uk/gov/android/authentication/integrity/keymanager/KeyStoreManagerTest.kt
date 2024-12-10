package uk.gov.android.authentication.integrity.keymanager

import java.math.BigInteger
import java.security.spec.ECField
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve
import kotlin.test.Test
import kotlin.test.assertEquals

class KeyStoreManagerTest {
    @Test
    fun `convert signature from DER format to ASN1 format`() {
        val spec = ECParameterSpec(
            EllipticCurve(
                ECField { return@ECField 256 },
                BigInteger(
                    "11579208921035624876269744694940757353008614341529031419553" +
                        "3631308867097853948"
                ),
                BigInteger(
                    "41058363725152142129326129780047268409114441015993725554" +
                        "835256314039467401291"
                )
            ),
            ECPoint(
                BigInteger(
                    "4843956129390645175905258525279791420276294952604174799584" +
                        "4080717082404635286"
                ),
                BigInteger(
                    "3613425095674979579858512791958788195661110667298501507187" +
                        "7198253568414405109"
                )
            ),
            BigInteger(
                "11579208921035624876269744694940757352999695522413576034242225" +
                    "9061068512044369"
            ),
            1
        )
        val input = byteArrayOf(
            48, 69, 2, 33, 0, -35, 79, -118, -2, -67, -74, 19, 59, 93, -39, -84,
            57, 11, -38, 35, -55, 101, 14, -16, -57, 72, -2, -67, -28, 119, 97, 98, 118, -66, 62,
            26, 120, 2, 32, 125, -110, -4, 61, -61, 53, -57, 20, -14, 96, 107, -13, -108, 12, -27,
            78, 91, -103, 13, -17, 13, 85, -103, 42, 54, 122, 101, -28, -55, 26, 12, -48
        )
        val expected = byteArrayOf(
            -35, 79, -118, -2, -67, -74, 19, 59, 93, -39, -84, 57, 11, -38,
            35, -55, 101, 14, -16, -57, 72, -2, -67, -28, 119, 97, 98, 118, -66, 62, 26, 120, 125,
            -110, -4, 61, -61, 53, -57, 20, -14, 96, 107, -13, -108, 12, -27, 78, 91, -103, 13,
            -17, 13, 85, -103, 42, 54, 122, 101, -28, -55, 26, 12, -48
        )

        val result = KeyStoreManager.convertSignatureToASN1(input, spec)
        assertEquals(expected.contentToString(), result.contentToString())
    }
}
