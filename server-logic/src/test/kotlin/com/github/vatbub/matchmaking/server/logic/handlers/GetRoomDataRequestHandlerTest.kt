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

import com.github.vatbub.matchmaking.common.requests.GetRoomDataRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.logic.roomproviders.MemoryRoomProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetRoomDataRequestHandlerTest : RequestHandlerWithRoomProviderAccessTestSuperclass<GetRoomDataRequestHandler>() {
    override fun getCloneOf(instance: GetRoomDataRequestHandler) = GetRoomDataRequestHandler(instance.roomProvider)
    override fun newObjectUnderTest(roomProvider: RoomProvider) = GetRoomDataRequestHandler(roomProvider)

    private lateinit var handler: GetRoomDataRequestHandler
    private lateinit var roomProvider: RoomProvider

    @BeforeEach
    fun prepareHandler() {
        roomProvider = MemoryRoomProvider()
        handler = GetRoomDataRequestHandler(roomProvider)
    }

    @Test
    override fun handleTest() {
        val room = roomProvider.createNewRoom(TestUtils.getRandomHexString(TestUtils.defaultConnectionId))
        val request = GetRoomDataRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, room.id)
        val response = handler.handle(request, null, null)

        Assertions.assertNotNull(response.room)
        Assertions.assertEquals(room, response.room)
    }

    @Test
    fun handleRoomNotFoundTest() {
        val request =
                GetRoomDataRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        val response = handler.handle(request, null, null)

        Assertions.assertNull(response.room)
    }

    @Test
    override fun positiveCanHandleTest() {
        val request =
                GetRoomDataRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
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
                GetRoomDataRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, TestUtils.getRandomHexString())
        Assertions.assertTrue(handler.needsAuthentication(request))
    }

}
