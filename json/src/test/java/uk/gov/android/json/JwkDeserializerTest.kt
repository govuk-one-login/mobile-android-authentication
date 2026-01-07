package uk.gov.android.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import org.jose4j.jwk.JsonWebKey
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.android.json.jwt.JwkDeserializer
import kotlin.test.Test

class JwkDeserializerTest {
    private val context: JsonDeserializationContext = mock()
    private val sut = JwkDeserializer()

    private val jwk = "{\"crv\":\"P-256\",\"kid\":\"key-0\",\"kty\":\"EC\",\"x\":\"Shc8mJ6fcZik" +
        "hWM4ofHGSwXTkdqXM8GbPtRzPa7LttA=\",\"y\":\"OIhg/7rhWfmnWQEgAXzU8fCTggGrS3zj5x76a0l" +
        "rzJM=\"}"

    private val jsonValidObj = JsonParser.parseString(jwk).asJsonObject

    @Test
    fun testContextIsNull() {
        assertThrows<IllegalArgumentException> {
            sut.deserialize(
                json = jsonValidObj,
                typeOfT = JsonWebKey::class.java,
                context = null,
            )
        }
    }

    @Test
    fun testFailureCreatingJwk() {
        whenever(context.deserialize<Map<String, Any>>(any(), any())).thenReturn(
            mapOf(
                "crv" to "P-256",
                "kid" to "key-0",
                "kty" to "AES",
                "x" to "hc8mJ6fcZikhWM4ofHGSwXTkdqXM8GbPtRzPa7LttA=",
                "y" to "OIhg/7rhWfmnWQEgAXzU8fCTggGrS3zj5x76a0lrzJM=",
            ),
        )
        assertThrows<JsonParseException> {
            sut.deserialize(
                json = jsonValidObj,
                typeOfT = JsonWebKey::class.java,
                context = context,
            )
        }
    }
}
