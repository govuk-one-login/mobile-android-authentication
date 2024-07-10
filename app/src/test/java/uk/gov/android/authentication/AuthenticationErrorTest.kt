package uk.gov.android.authentication

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuthenticationErrorTest {
    @Test
    fun testErrorMessageAndType() {
        val error = AuthenticationError("Invalid credentials", AuthenticationError.ErrorType.OAUTH)
        assertEquals("Invalid credentials", error.message)
        assertEquals(AuthenticationError.ErrorType.OAUTH, error.type)
    }
}
