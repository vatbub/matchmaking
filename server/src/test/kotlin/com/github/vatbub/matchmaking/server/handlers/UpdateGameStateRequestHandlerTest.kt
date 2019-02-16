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
package com.github.vatbub.matchmaking.server.handlers

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.requests.UpdateGameStateRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException
import com.github.vatbub.matchmaking.server.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UpdateGameStateRequestHandlerTest : RequestHandlerTestSuperclass() {
    @Test
    override fun handleTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = UpdateGameStateRequestHandler(roomProvider)
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        val dataToHost1 = GameData()
        dataToHost1["host1Key"] = "host1Value"
        val dataToHost2 = GameData()
        dataToHost2["host2Key"] = "host2Value"

        val transaction = roomProvider.beginTransactionWithRoom(room.id)!!
        transaction.room.dataToBeSentToTheHost.add(dataToHost1)
        transaction.room.dataToBeSentToTheHost.add(dataToHost2)
        transaction.commit()

        val modifiedRoom = roomProvider[room.id]!!
        Assertions.assertEquals(2, modifiedRoom.dataToBeSentToTheHost.size)

        val newGameState = GameData()
        newGameState["newKey"] = "newValue"

        val request =
            UpdateGameStateRequest(
                TestUtils.defaultConnectionId,
                TestUtils.defaultPassword,
                room.id,
                newGameState,
                listOf(dataToHost1)
            )
        val response = handler.handle(request, null, null)

        response as GetRoomDataResponse
        Assertions.assertEquals(newGameState, response.room!!.gameState)
        Assertions.assertEquals(1, response.room!!.dataToBeSentToTheHost.size)
        Assertions.assertEquals(dataToHost2, response.room!!.dataToBeSentToTheHost[0])
    }

    @Test
    fun handleNonExistingRoomTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = UpdateGameStateRequestHandler(roomProvider)

        val request = UpdateGameStateRequest(
            TestUtils.defaultConnectionId,
            TestUtils.defaultPassword,
            TestUtils.getRandomHexString(),
            GameData(),
            listOf()
        )
        val response = handler.handle(request, null, null)

        response as GetRoomDataResponse
        Assertions.assertNull(response.room)
    }

    @Test
    fun handleNotAllowedTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = UpdateGameStateRequestHandler(roomProvider)
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)

        Assertions.assertFalse(room.gameStarted)

        val connectionId = TestUtils.getRandomHexString(TestUtils.defaultConnectionId)
        val request = UpdateGameStateRequest(connectionId, TestUtils.defaultPassword, room.id, GameData(), listOf())
        val response = handler.handle(request, null, null)

        response as NotAllowedException
        Assertions.assertEquals(
            "Unable to set the game state: The sending client is not the host, only a game host can start the game",
            response.message
        )
        Assertions.assertEquals(connectionId, response.connectionId)
    }

    @Test
    override fun positiveCanHandleTest() {
        val handler = UpdateGameStateRequestHandler(MemoryRoomProvider())
        val request = UpdateGameStateRequest(
            TestUtils.defaultConnectionId,
            TestUtils.defaultPassword,
            TestUtils.getRandomHexString(),
            GameData(),
            listOf()
        )
        Assertions.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val handler = UpdateGameStateRequestHandler(MemoryRoomProvider())
        val request = DummyRequest()
        Assertions.assertFalse(handler.canHandle(request))
    }

    @Test
    override fun needsAuthenticationTest() {
        val handler = UpdateGameStateRequestHandler(MemoryRoomProvider())
        val request = UpdateGameStateRequest(
            TestUtils.defaultConnectionId,
            TestUtils.defaultPassword,
            TestUtils.getRandomHexString(),
            GameData(),
            listOf()
        )
        Assertions.assertTrue(handler.needsAuthentication(request))
    }
}
