package com.github.vatbub.matchmaking.server.handlers

import com.github.vatbub.matchmaking.common.requests.GetConnectionIdRequest
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.server.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.idprovider.MemoryIdProvider
import org.junit.Assert
import org.junit.jupiter.api.Test

class GetConnectionIdHandlerTest : RequestHandlerTestSuperclass() {
    @Test
    override fun positiveCanHandleTest() {
        val request = GetConnectionIdRequest()
        val handler = GetConnectionIdHandler(MemoryIdProvider())
        Assert.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val request = DummyRequest()
        val handler = GetConnectionIdHandler(MemoryIdProvider())
        Assert.assertFalse(handler.canHandle(request))
    }

    @Test
    override fun handleTest() {
        val idProvider = MemoryIdProvider()
        val handler = GetConnectionIdHandler(idProvider)
        val request = GetConnectionIdRequest()
        val response = handler.handle(request)

        Assert.assertTrue(response is GetConnectionIdResponse)
        Assert.assertEquals(idProvider.connectionIdsInUse[idProvider.connectionIdsInUse.size - 1], response.connectionId)
    }
}