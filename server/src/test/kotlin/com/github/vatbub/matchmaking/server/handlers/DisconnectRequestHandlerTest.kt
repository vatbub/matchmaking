package com.github.vatbub.matchmaking.server.handlers

import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.requests.DisconnectRequest
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.server.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DisconnectRequestHandlerTest : RequestHandlerTestSuperclass() {
    @Test
    override fun handleTest() {
        val roomProvider = MemoryRoomProvider()

        val hostedRooms = listOf(
            roomProvider.createNewRoom(TestUtils.defaultConnectionId),
            roomProvider.createNewRoom(TestUtils.defaultConnectionId),
            roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        )

        val connectedRooms = listOf(
            roomProvider.createNewRoom(TestUtils.getRandomHexString(TestUtils.defaultConnectionId)),
            roomProvider.createNewRoom(TestUtils.getRandomHexString(TestUtils.defaultConnectionId)),
            roomProvider.createNewRoom(TestUtils.getRandomHexString(TestUtils.defaultConnectionId))
        )

        for (room in connectedRooms) {
            room.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))
            roomProvider.commitChangesToRoom(room)
        }

        val request = DisconnectRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val handler = DisconnectRequestHandler(roomProvider)

        val response = handler.handle(request, null, null)

        Assertions.assertTrue(response is DisconnectResponse)
        response as DisconnectResponse
        Assert.assertArrayEquals(hostedRooms.toTypedArray(), response.destroyedRooms.toTypedArray())
        Assert.assertArrayEquals(connectedRooms.toTypedArray(), response.disconnectedRooms.toTypedArray())
    }

    @Test
    override fun positiveCanHandleTest() {
        val request = DisconnectRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val handler = DisconnectRequestHandler(MemoryRoomProvider())
        Assertions.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val request = DummyRequest()
        val handler = DisconnectRequestHandler(MemoryRoomProvider())
        Assertions.assertFalse(handler.canHandle(request))
    }

    @Test
    override fun needsAuthenticationTest() {
        val request = DisconnectRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val handler = DisconnectRequestHandler(MemoryRoomProvider())
        Assertions.assertTrue(handler.needsAuthentication(request))
    }
}