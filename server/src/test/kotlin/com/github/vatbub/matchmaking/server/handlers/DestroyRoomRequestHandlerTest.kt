package com.github.vatbub.matchmaking.server.handlers

import com.github.vatbub.matchmaking.common.requests.DestroyRoomRequest
import com.github.vatbub.matchmaking.common.responses.DestroyRoomResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException
import com.github.vatbub.matchmaking.server.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DestroyRoomRequestHandlerTest : RequestHandlerTestSuperclass() {
    @Test
    override fun handleTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = DestroyRoomRequestHandler(roomProvider)

        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val request = DestroyRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, room.id)
        val response = handler.handle(request, null, null)

        Assertions.assertTrue(response is DestroyRoomResponse)
        response as DestroyRoomResponse
        Assert.assertTrue(response.roomDestroyed)
    }

    @Test
    fun handleNotHostTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = DestroyRoomRequestHandler(roomProvider)

        val room = roomProvider.createNewRoom(TestUtils.getRandomHexString(TestUtils.defaultConnectionId))

        val request = DestroyRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, room.id)
        val response = handler.handle(request, null, null)

        Assertions.assertTrue(response is NotAllowedException)
        response as NotAllowedException
        Assertions.assertEquals(
            response.message,
            "The sender's connection id does not equal the room's host connection id. Only the host can destroy a room."
        )
    }

    @Test
    fun handleRoomNotFoundTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = DestroyRoomRequestHandler(roomProvider)

        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val request = DestroyRoomRequest(
            TestUtils.defaultConnectionId,
            TestUtils.defaultPassword,
            TestUtils.getRandomHexString(room.id)
        )
        val response = handler.handle(request, null, null)

        Assertions.assertTrue(response is DestroyRoomResponse)
        response as DestroyRoomResponse
        Assert.assertFalse(response.roomDestroyed)
    }

    @Test
    override fun positiveCanHandleTest() {
        val request =
            DestroyRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        val handler = DestroyRoomRequestHandler(MemoryRoomProvider())
        Assertions.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val request = DummyRequest()
        val handler = DestroyRoomRequestHandler(MemoryRoomProvider())
        Assertions.assertFalse(handler.canHandle(request))
    }

    @Test
    override fun needsAuthenticationTest() {
        val request =
            DestroyRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        val handler = DestroyRoomRequestHandler(MemoryRoomProvider())
        Assertions.assertTrue(handler.needsAuthentication(request))
    }
}