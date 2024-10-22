package uk.gov.android.wallet.core.issuer.verify.jwt

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.jose4j.jwk.JsonWebKey
import org.jose4j.lang.JoseException
import java.lang.reflect.Type

class JwkDeserializer : JsonDeserializer<JsonWebKey> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): JsonWebKey {
        val jwkParameters: Map<String, Any> = context?.deserialize(
            json,
            LinkedHashMap::class.java
        ) ?: throw IllegalArgumentException("JsonDeserializationContext is null")
        return try {
            JsonWebKey.Factory.newJwk(jwkParameters)
        } catch (e: JoseException) {
            throw JsonParseException("Unable to create JWK Object when parsing JSON", e)
        }
    }
}
