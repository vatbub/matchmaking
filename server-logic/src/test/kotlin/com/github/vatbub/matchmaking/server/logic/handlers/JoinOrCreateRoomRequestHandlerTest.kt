/*-
 * #%L
 * matchmaking.server
 * %%
 * Copyright (C) 2016 - 2019 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.matchmaking.server.logic.handlers

import com.github.vatbub.matchmaking.common.requests.JoinOrCreateRoomRequest
import com.github.vatbub.matchmaking.common.requests.Operation
import com.github.vatbub.matchmaking.common.responses.JoinOrCreateRoomResponse
import com.github.vatbub.matchmaking.common.responses.Result
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JoinOrCreateRoomRequestHandlerTest : RequestHandlerWithRoomProviderAccessTestSuperclass<JoinOrCreateRoomRequestHandler>() {
    override fun getCloneOf(instance: JoinOrCreateRoomRequestHandler) = JoinOrCreateRoomRequestHandler(instance.roomProvider)
    override fun newObjectUnderTest(roomProvider: RoomProvider) = JoinOrCreateRoomRequestHandler(roomProvider)

    private lateinit var handler: JoinOrCreateRoomRequestHandler
    private lateinit var roomProvider: RoomProvider

    @BeforeEach
    fun prepareHandler() {
        roomProvider = MemoryRoomProvider()
        handler = JoinOrCreateRoomRequestHandler(roomProvider)
    }

    @Test
    override fun handleTest() {
        val request1 = JoinOrCreateRoomRequest(
                TestUtils.defaultConnectionId,
                TestUtils.defaultPassword,
                Operation.JoinOrCreateRoom,
                "vatbub"
        )

        val response1 = handler.handle(request1, null, null)

        Assertions.assertEquals(200, response1.httpStatusCode)
        Assertions.assertTrue(response1 is JoinOrCreateRoomResponse)
        response1 as JoinOrCreateRoomResponse
        Assertions.assertEquals(Result.RoomCreated, response1.result)
        Assertions.assertNotNull(response1.roomId)
        val roomId = response1.roomId!!
        Assertions.assertEquals(1, roomProvider[roomId]!!.connectedUsers.size)
        Assertions.assertEquals(request1.connectionId, roomProvider[roomId]!!.connectedUsers[0].connectionId)
        Assertions.assertEquals(request1.userName, roomProvider[roomId]!!.connectedUsers[0].userName)

        val request2 = JoinOrCreateRoomRequest(
                TestUtils.getRandomHexString(TestUtils.defaultConnectionId),
                TestUtils.getRandomHexString(TestUtils.defaultPassword),
                Operation.JoinOrCreateRoom,
                "heykey"
        )

        val response2 = handler.handle(request2, null, null)

        Assertions.assertEquals(200, response2.httpStatusCode)
        Assertions.assertTrue(response2 is JoinOrCreateRoomResponse)
        response2 as JoinOrCreateRoomResponse
        Assertions.assertEquals(Result.RoomJoined, response2.result)
        Assertions.assertNotNull(response2.roomId)
        Assertions.assertEquals(roomId, response2.roomId)
        Assertions.assertEquals(2, roomProvider[roomId]!!.connectedUsers.size)
        Assertions.assertEquals(request2.connectionId, roomProvider[roomId]!!.connectedUsers[1].connectionId)
        Assertions.assertEquals(request2.userName, roomProvider[roomId]!!.connectedUsers[1].userName)
    }

    @Test
    fun handleRequestWithResultNothingTest() {
        val request = JoinOrCreateRoomRequest(
                TestUtils.defaultConnectionId,
                TestUtils.defaultPassword,
                Operation.JoinRoom,
                "vatbub",
                maxRoomSize = 2
        )

        val response = handler.handle(request, null, null)

        Assertions.assertEquals(200, response.httpStatusCode)
        Assertions.assertTrue(response is JoinOrCreateRoomResponse)
        response as JoinOrCreateRoomResponse
        Assertions.assertEquals(Result.Nothing, response.result)
        Assertions.assertNull(response.roomId)
    }

    @Test
    override fun positiveCanHandleTest() {
        val request =
                JoinOrCreateRoomRequest(
                        TestUtils.defaultConnectionId,
                        TestUtils.defaultPassword,
                        Operation.JoinOrCreateRoom,
                        "vatbub"
                )
        Assertions.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val request = DummyRequest()
        Assertions.assertFalse(handler.canHandle(request))
    }

    @Test
    override fun needsAuthenticationTest() {
        val request =
                JoinOrCreateRoomRequest(
                        TestUtils.defaultConnectionId,
                        TestUtils.defaultPassword,
                        Operation.JoinOrCreateRoom,
                        "vatbub"
                )
        Assertions.assertTrue(handler.needsAuthentication(request))
    }
}
