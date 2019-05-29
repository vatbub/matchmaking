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
package com.github.vatbub.matchmaking.server

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse
import com.github.vatbub.matchmaking.server.logic.ServerContext
import com.github.vatbub.matchmaking.server.logic.testing.dummies.DummyRequestHandler
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.websocket.CloseReason
import javax.websocket.EndpointConfig

class WebsocketEndpointTest : KotlinTestSuperclass<WebsocketEndpoint>() {
    override fun getCloneOf(instance: WebsocketEndpoint) = WebsocketEndpoint(instance.serverContext)

    override fun newObjectUnderTest() = WebsocketEndpoint()

    private lateinit var websocketEndpoint: WebsocketEndpoint
    private lateinit var websocketSession: MockSession
    private lateinit var websocketEndpointConfig: EndpointConfig

    @BeforeEach
    fun instantiateAndCallOpen() {
        websocketEndpoint = newObjectUnderTest()
        websocketSession = MockSession()
        websocketEndpointConfig = MockEndpointConfig()
        websocketEndpoint.open(websocketSession, websocketEndpointConfig)
    }

    @AfterEach
    fun callOnSessionClose() {
        websocketEndpoint.onSessionClose(websocketSession, CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Test finished"))
    }

    private fun sendRequest(request: Request): Response {
        val serializedRequest = InteractionConverter.serialize(request)
        val serializedResponse = sendRequest(serializedRequest)
        return InteractionConverter.deserializeResponse(serializedResponse)
    }

    private fun sendRequest(request: String): String {
        websocketSession.mockBasicRemote.textData.clear()
        websocketEndpoint.onTextMessage(websocketSession, request)
        return websocketSession.mockBasicRemote.textData.joinToString("\n")
    }

    @Test
    fun positiveHandleTest() {
        websocketEndpoint.serverContext.messageDispatcher.removeAllHandlers()
        websocketEndpoint.serverContext.messageDispatcher.registerHandler(DummyRequestHandler())
        val request = DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val response = sendRequest(request)
        Assertions.assertTrue(response is DummyResponse)
        response as DummyResponse
        Assertions.assertEquals(request.connectionId, response.connectionId)
    }

    @Test
    override fun notEqualsTest() {
        val context1 = ServerContext()
        val context2 = ServerContext()
        context2.connectionIdProvider.getNewId()
        val websocketEndpoint1 = WebsocketEndpoint(context1)
        val websocketEndpoint2 = WebsocketEndpoint(context2)
        Assertions.assertNotEquals(websocketEndpoint1, websocketEndpoint2)
    }
}
