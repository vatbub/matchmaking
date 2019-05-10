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

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.requests.SendDataToHostRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SendDataToHostRequestHandlerTest : RequestHandlerTestSuperclass<SendDataToHostRequestHandler>() {
    override fun newObjectUnderTest() = SendDataToHostRequestHandler(MemoryRoomProvider())

    @Test
    override fun handleTest() {
        val roomProvider = MemoryRoomProvider()
        val handler = SendDataToHostRequestHandler(roomProvider)
        val room = roomProvider.createNewRoom(TestUtils.defaultConnectionId)
        val gameDataToBeSent = listOf(GameData(TestUtils.defaultConnectionId))
        val request =
                SendDataToHostRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, room.id, gameDataToBeSent)
        val response = handler.handle(request, null, null)

        Assertions.assertTrue(response is GetRoomDataResponse)
        response as GetRoomDataResponse
        val modifiedRoom = response.room!!

        Assertions.assertEquals(
                room.dataToBeSentToTheHost.size + gameDataToBeSent.size,
                modifiedRoom.dataToBeSentToTheHost.size
        )
        Assertions.assertEquals(gameDataToBeSent, modifiedRoom.dataToBeSentToTheHost)
    }

    @Test
    fun handleNonExistingRoomTest() {
        val handler = SendDataToHostRequestHandler(MemoryRoomProvider())
        val gameDataToBeSent = listOf(GameData(TestUtils.defaultConnectionId))
        val request =
                SendDataToHostRequest(
                        TestUtils.defaultConnectionId,
                        TestUtils.defaultPassword,
                        TestUtils.getRandomHexString(),
                        gameDataToBeSent
                )
        val response = handler.handle(request, null, null)

        Assertions.assertTrue(response is GetRoomDataResponse)
        response as GetRoomDataResponse
        Assertions.assertNull(response.room)
    }

    @Test
    override fun positiveCanHandleTest() {
        val handler = SendDataToHostRequestHandler(MemoryRoomProvider())
        val request = SendDataToHostRequest(
                TestUtils.defaultConnectionId,
                TestUtils.defaultPassword,
                TestUtils.getRandomHexString(),
                listOf()
        )
        Assertions.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val handler = SendDataToHostRequestHandler(MemoryRoomProvider())
        Assertions.assertFalse(handler.canHandle(DummyRequest()))
    }

    @Test
    override fun needsAuthenticationTest() {
        val handler = SendDataToHostRequestHandler(MemoryRoomProvider())
        val request = SendDataToHostRequest(
                TestUtils.defaultConnectionId,
                TestUtils.defaultPassword,
                TestUtils.getRandomHexString(),
                listOf()
        )
        Assertions.assertTrue(handler.needsAuthentication(request))
    }
}
