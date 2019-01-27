package com.github.vatbub.matchmaking.common.serializationtests

import com.github.vatbub.matchmaking.common.ServerInteraction
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

abstract class ServerInteractionSerializationTestSuperclass<T : ServerInteraction>(clazz: Class<T>) :
    SerializationTestSuperclass<T>(clazz) {

    @Test
    fun protocolVersionTest() {
        Assertions.assertEquals(ServerInteraction.defaultProtocolVersion, newObjectUnderTest().protocolVersion)
    }
}