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

import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.requests.DisconnectRequest
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DisconnectRequestHandlerTest : RequestHandlerWithRoomProviderAccessTestSuperclass<DisconnectRequestHandler>() {
    override fun getCloneOf(instance: DisconnectRequestHandler) = DisconnectRequestHandler(instance.roomProvider)
    override fun newObjectUnderTest() = newObjectUnderTest(MemoryRoomProvider())
    override fun newObjectUnderTest(roomProvider: RoomProvider) = DisconnectRequestHandler(roomProvider)

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
            val roomTransaction = roomProvider.beginTransactionWithRoom(room.id) ?: continue
            roomTransaction.room.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))
            roomTransaction.commit()
            Assertions.assertEquals(1, roomProvider[room.id]!!.connectedUsers.size)
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
