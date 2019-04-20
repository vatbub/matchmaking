package com.github.vatbub.matchmaking.common

import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultConnectionId
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultPassword
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InteractionConverterDeserializationTestsTest : KotlinTestSuperclass() {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    @Test
    fun requestCastTest() {
        val request = DummyRequest(defaultConnectionId, defaultPassword)
        val serializedRequest = gson.toJson(request)
        var deserializedRequest: DummyRequest? = null
        Assertions.assertDoesNotThrow {
            deserializedRequest = InteractionConverter.deserializeRequest(serializedRequest)
        }
        Assertions.assertEquals(request, deserializedRequest)
    }

    @Test
    fun responseCastTest() {
        val response = DummyResponse(defaultConnectionId)
        val serializedResponse = gson.toJson(response)
        var deserializedResponse: DummyResponse? = null
        Assertions.assertDoesNotThrow {
            deserializedResponse = InteractionConverter.deserializeResponse(serializedResponse)
        }
        Assertions.assertEquals(response, deserializedResponse)
    }
}