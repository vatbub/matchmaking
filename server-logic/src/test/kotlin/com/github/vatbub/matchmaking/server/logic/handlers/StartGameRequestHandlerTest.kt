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

import com.github.vatbub.matchmaking.common.requests.StartGameRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class StartGameRequestHandlerTest : RequestHandlerWithRoomProviderAccessTestSuperclass<StartGameRequestHandler>() {
    override fun getCloneOf(instance: StartGameRequestHandler) = StartGameRequestHandler(instance.roomProvider)
    override fun newObjectUnderTest(roomProvider: RoomProvider) = StartGameRequestHandler(roomProvider)

    @Test
    override fun handleTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = StartGameRequestHandler(roomProvider)
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        Assertions.assertFalse(room.gameStarted)

        val request = StartGameRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, room.id)
        val response = handler.handle(request, null, null)

        response as GetRoomDataResponse
        Assertions.assertTrue(response.room!!.gameStarted)
    }

    @Test
    fun handleNonExistingRoomTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = StartGameRequestHandler(roomProvider)

        val request =
                StartGameRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        val response = handler.handle(request, null, null)

        response as GetRoomDataResponse
        Assertions.assertNull(response.room)
    }

    @Test
    fun handleNotAllowedTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = StartGameRequestHandler(roomProvider)
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        Assertions.assertFalse(room.gameStarted)

        val connectionId = TestUtils.getRandomHexString(TestUtils.defaultConnectionId)
        val request = StartGameRequest(connectionId, TestUtils.defaultPassword, room.id)
        val response = handler.handle(request, null, null)

        response as NotAllowedException
        Assertions.assertEquals(
                "Unable to start the game: The sending client is not the host, only a game host can start the game",
                response.message
        )
        Assertions.assertEquals(connectionId, response.connectionId)
    }

    @Test
    override fun positiveCanHandleTest() {
        val handler = StartGameRequestHandler(MemoryRoomProvider())
        val request =
                StartGameRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        Assertions.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val handler = StartGameRequestHandler(MemoryRoomProvider())
        val request = DummyRequest()
        Assertions.assertFalse(handler.canHandle(request))
    }

    @Test
    override fun needsAuthenticationTest() {
        val handler = StartGameRequestHandler(MemoryRoomProvider())
        val request =
                StartGameRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        Assertions.assertTrue(handler.needsAuthentication(request))
    }
}
