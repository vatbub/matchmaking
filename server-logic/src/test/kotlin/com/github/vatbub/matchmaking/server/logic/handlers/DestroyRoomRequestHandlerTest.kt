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

import com.github.vatbub.matchmaking.common.requests.DestroyRoomRequest
import com.github.vatbub.matchmaking.common.responses.DestroyRoomResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
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
