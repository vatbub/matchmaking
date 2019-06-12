package com.github.vatbub.matchmaking.server.logic

import com.github.vatbub.matchmaking.server.logic.configuration.JndiHelper
import javax.naming.InitialContext
import org.mockito.Mockito.*

object JndiTestUtils {
    fun <T> mockContext(environment: Map<String, T>, prefixKeys: Boolean = true) {
        val context = mock(InitialContext::class.java)
        environment.forEach { (key, value) -> `when`(context.lookup(generatePrefixedKeyIfApplicable(key, prefixKeys))).thenReturn(value) }
        JndiHelper.context = context
    }

    private fun generatePrefixedKeyIfApplicable(key: String, prefix: Boolean) =
            if (prefix)
                "java:comp/env/$key"
            else
                key

    fun resetContext() {
        JndiHelper.context = InitialContext()
    }
}