package com.github.vatbub.matchmaking.server.logic.handlers

import com.github.vatbub.matchmaking.common.ServerInteraction
import com.github.vatbub.matchmaking.common.requests.SubscribeToRoomRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.SubscribeToRoomResponse
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.server.logic.sockets.Session
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SubscribeToRoomRequestHandlerTest : RequestHandlerTestSuperclass() {
    @Test
    override fun handleTest() {
        val handler = SubscribeToRoomRequestHandler(MemoryRoomProvider())
        val request = SubscribeToRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        Assertions.assertThrows(IllegalStateException::class.java) { handler.handle(request, null, null) }
    }

    @Test
    override fun positiveCanHandleTest() {
        val handler = SubscribeToRoomRequestHandler(MemoryRoomProvider())
        val request = SubscribeToRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        Assertions.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val handler = SubscribeToRoomRequestHandler(MemoryRoomProvider())
        val request = DummyRequest()
        Assertions.assertFalse(handler.canHandle(request))
    }

    @Test
    override fun needsAuthenticationTest() {
        val handler = SubscribeToRoomRequestHandler(MemoryRoomProvider())
        val request = SubscribeToRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        Assertions.assertTrue(handler.needsAuthentication(request))
    }

    @Test
    fun handleRequestOverSocketTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = SubscribeToRoomRequestHandler(roomProvider)
        val request = SubscribeToRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        val session = object : Session() {
            override fun sendObjectSync(objectToSend: ServerInteraction) {}
            override fun sendObjectAsync(objectToSend: ServerInteraction) {}
        }
        val response = handler.handle(session, request, null, null)

        Assertions.assertTrue(response is SubscribeToRoomResponse)
        Assertions.assertEquals(request.connectionId, response.connectionId)
        Assertions.assertEquals(1, roomProvider.onCommitRoomTransactionListeners.size)
    }

    @Test
    fun onSessionCLoseTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = SubscribeToRoomRequestHandler(roomProvider)
        val request = SubscribeToRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        val session = object : Session() {
            override fun sendObjectSync(objectToSend: ServerInteraction) {}
            override fun sendObjectAsync(objectToSend: ServerInteraction) {}
        }
        handler.handle(session, request, null, null)
        handler.onSessionClosed(session)
        Assertions.assertEquals(0, roomProvider.onCommitRoomTransactionListeners.size)
    }

    @Test
    fun subscribeTest() {
        val roomProvider = MemoryRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.getRandomHexString())
        val handler = SubscribeToRoomRequestHandler(roomProvider)
        val request = SubscribeToRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, room.id)

        var sessionCalled = false
        val session = object : Session() {
            override fun sendObjectAsync(objectToSend: ServerInteraction) {
                sendObjectSync(objectToSend)
            }

            override fun sendObjectSync(objectToSend: ServerInteraction) {
                sessionCalled = true
                Assertions.assertTrue(objectToSend is GetRoomDataResponse)
                objectToSend as GetRoomDataResponse
                Assertions.assertNotNull(objectToSend.room)
                Assertions.assertTrue(objectToSend.room?.gameStarted ?: false)
            }
        }
        handler.handle(session, request, null, null)

        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!
        transaction.room.gameStarted = true
        transaction.commit()

        Assertions.assertTrue(sessionCalled)
    }

    @Test
    fun subscribeToOtherRoom() {
        val roomProvider = MemoryRoomProvider()
        val room = roomProvider.createNewRoom(TestUtils.getRandomHexString())
        val handler = SubscribeToRoomRequestHandler(roomProvider)
        val request = SubscribeToRoomRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString(room.id))

        val session = object : Session() {
            override fun sendObjectAsync(objectToSend: ServerInteraction) {
                sendObjectSync(objectToSend)
            }

            override fun sendObjectSync(objectToSend: ServerInteraction) {
                Assertions.fail<Void>("Session should not be called")
            }
        }
        handler.handle(session, request, null, null)

        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!
        transaction.room.gameStarted = true
        transaction.commit()
    }
}