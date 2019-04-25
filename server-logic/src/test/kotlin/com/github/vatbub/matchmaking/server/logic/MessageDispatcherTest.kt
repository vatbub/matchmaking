/*-
 * #%L
 * matchmaking.server
 * %%
 * Copyright (C) 2016 - 2018 Frederik Kammel
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
package com.github.vatbub.matchmaking.server.logic

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.ServerInteraction
import com.github.vatbub.matchmaking.common.responses.AuthorizationException
import com.github.vatbub.matchmaking.common.responses.BadRequestException
import com.github.vatbub.matchmaking.common.responses.InternalServerErrorException
import com.github.vatbub.matchmaking.common.responses.UnknownConnectionIdException
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse
import com.github.vatbub.matchmaking.server.logic.handlers.RequestHandlerWithWebsocketSupport
import com.github.vatbub.matchmaking.server.logic.idprovider.MemoryIdProvider
import com.github.vatbub.matchmaking.server.logic.sockets.Session
import com.github.vatbub.matchmaking.server.logic.testing.dummies.DummyRequestHandler
import com.github.vatbub.matchmaking.server.logic.testing.dummies.DynamicRequestHandler
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.Inet4Address
import java.net.Inet6Address

class MessageDispatcherTest : KotlinTestSuperclass() {
    @Test
    fun registerSameHandlerTwiceTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val handler = DummyRequestHandler()

        messageDispatcher.registerHandler(handler)
        messageDispatcher.registerHandler(handler)

        Assertions.assertEquals(1, messageDispatcher.handlers.size)
    }

    @Test
    fun registerHandlerTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val handler = DummyRequestHandler()

        Assertions.assertFalse(messageDispatcher.isHandlerRegistered(handler))
        messageDispatcher.registerHandler(handler)
        Assertions.assertTrue(messageDispatcher.isHandlerRegistered(handler))
    }

    @Test
    fun positiveRemoveHandlerTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val handler = DummyRequestHandler()
        messageDispatcher.registerHandler(handler)
        Assertions.assertTrue(messageDispatcher.removeHandler(handler))
        Assertions.assertFalse(messageDispatcher.isHandlerRegistered(handler))
    }

    @Test
    fun negativeRemoveHandlerTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val handler = DummyRequestHandler()
        Assertions.assertFalse(messageDispatcher.removeHandler(handler))
        Assertions.assertFalse(messageDispatcher.isHandlerRegistered(handler))
    }

    @Test
    fun positiveDispatchTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val handler = DummyRequestHandler()
        messageDispatcher.registerHandler(handler)

        val request = DummyRequest(
                TestUtils.defaultConnectionId,
                TestUtils.defaultPassword
        )
        val response = messageDispatcher.dispatch(request, null, null)

        Assertions.assertNotNull(response)
        Assertions.assertTrue(handler.handledRequests.isNotEmpty())
        Assertions.assertSame(handler.handledRequests[request], response)
    }

    @Test
    fun negativeDispatchTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val handler = DummyRequestHandler()

        val request = DummyRequest(
                TestUtils.defaultConnectionId,
                TestUtils.defaultPassword
        )
        val response = messageDispatcher.dispatch(request, null, null)

        Assertions.assertNull(response)
        Assertions.assertTrue(handler.handledRequests.isEmpty())
    }

    @Test
    fun authorizationNotFoundTest() {
        val idProvider = MemoryIdProvider()
        val messageDispatcher = MessageDispatcher(idProvider)
        val handler = DummyRequestHandler(needsAuthentication = true)
        messageDispatcher.registerHandler(handler)

        val id = idProvider.getNewId()
        val request =
                DummyRequest(
                        TestUtils.getRandomHexString(id.connectionId),
                        TestUtils.getRandomHexString(id.password)
                )
        val response = messageDispatcher.dispatch(request, null, null)

        Assertions.assertNotNull(response)
        Assertions.assertTrue(response is UnknownConnectionIdException)
        response as UnknownConnectionIdException
        Assertions.assertEquals("The specified connection id is not known to the server", response.message)
        Assert.assertTrue(handler.handledRequests.isEmpty())
    }

    @Test
    fun notAuthorizedTest() {
        val idProvider = MemoryIdProvider()
        val messageDispatcher = MessageDispatcher(idProvider)
        val handler = DummyRequestHandler(needsAuthentication = true)
        messageDispatcher.registerHandler(handler)

        val id = idProvider.getNewId()
        val request = DummyRequest(
                id.connectionId,
                TestUtils.getRandomHexString(id.password)
        )
        val response = messageDispatcher.dispatch(request, null, null)

        Assertions.assertNotNull(response)
        Assertions.assertTrue(response is AuthorizationException)
        response as AuthorizationException
        Assertions.assertEquals("Incorrect password", response.message)
        Assert.assertTrue(handler.handledRequests.isEmpty())
    }

    @Test
    fun authorizedTest() {
        val idProvider = MemoryIdProvider()
        val messageDispatcher = MessageDispatcher(idProvider)
        val handler = DummyRequestHandler(needsAuthentication = true)
        messageDispatcher.registerHandler(handler)

        val id = idProvider.getNewId()
        val request = DummyRequest(id.connectionId, id.password)
        val response = messageDispatcher.dispatch(request, null, null)

        Assertions.assertNotNull(response)
        Assertions.assertTrue(handler.handledRequests.isNotEmpty())
        Assertions.assertSame(handler.handledRequests[request], response)
    }

    @Test
    fun removeAllHandlersTest() {
        val idProvider = MemoryIdProvider()
        val messageDispatcher = MessageDispatcher(idProvider)
        messageDispatcher.registerHandler(DummyRequestHandler())

        Assertions.assertEquals(1, messageDispatcher.handlers.size)
        messageDispatcher.removeAllHandlers()
        Assertions.assertEquals(0, messageDispatcher.handlers.size)
    }

    @Test
    fun dispatchOrCreateExceptionThrowIllegalArgumentExceptionTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val exceptionMessage = "A sample exception"
        val requestId = TestUtils.getRandomHexString()
        val handler = DynamicRequestHandler<DummyRequest>({ true }, { false }, { _, _, _ -> throw IllegalArgumentException(exceptionMessage) })
        messageDispatcher.registerHandler(handler)
        val response = messageDispatcher.dispatchOrCreateException(DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, requestId), null, null, null)
        response as BadRequestException
        Assertions.assertEquals(IllegalArgumentException::class.java.name + ", " + exceptionMessage, response.message)
        Assertions.assertEquals(requestId, response.responseTo)
    }

    @Test
    fun dispatchOrCreateExceptionThrowExceptionTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val exceptionMessage = "A sample exception"
        val requestId = TestUtils.getRandomHexString()
        val handler = DynamicRequestHandler<DummyRequest>({ true }, { false }, { _, _, _ -> throw Exception(exceptionMessage) })
        messageDispatcher.registerHandler(handler)
        val response = messageDispatcher.dispatchOrCreateException(DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, requestId), null, null, null)
        response as InternalServerErrorException
        Assertions.assertEquals(Exception::class.java.name + ", " + exceptionMessage, response.message)
        Assertions.assertEquals(requestId, response.responseTo)
    }

    @Test
    fun dispatchOrCreateExceptionNoHandlersTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val exceptionMessage = "No response generated by server"
        val requestId = TestUtils.getRandomHexString()
        val response = messageDispatcher.dispatchOrCreateException(DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, requestId), null, null, null)
        response as InternalServerErrorException
        Assertions.assertEquals(IllegalStateException::class.java.name + ", " + exceptionMessage, response.message)
        Assertions.assertEquals(requestId, response.responseTo)
    }

    @Test
    fun positiveDispatchOrCreateExceptionTest() {
        val requestId = TestUtils.getRandomHexString()
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val handler = DummyRequestHandler()
        messageDispatcher.registerHandler(handler)

        val request = DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, requestId)
        val response = messageDispatcher.dispatchOrCreateException(request, null, null)

        Assertions.assertNotNull(response)
        Assertions.assertTrue(handler.handledRequests.isNotEmpty())
        Assertions.assertSame(handler.handledRequests[request], response)
        Assertions.assertEquals(requestId, response.responseTo)
    }

    @Test
    fun dispatchWebsocketSessionClosedTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())

        val expectedSession = object : Session() {
            override fun sendObjectSync(objectToSend: ServerInteraction) {}
            override fun sendObjectAsync(objectToSend: ServerInteraction) {}
        }

        var onSessionClosedCalled = false
        val handler = object : RequestHandlerWithWebsocketSupport<DummyRequest>() {
            override val requiresSocket = false
            override fun handle(session: Session, request: DummyRequest, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?) = DummyResponse(request.connectionId)
            override fun handle(request: DummyRequest, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?) = DummyResponse(request.connectionId)
            override fun canHandle(request: Request) = true
            override fun needsAuthentication(request: DummyRequest) = false

            override fun onSessionClosed(session: Session) {
                onSessionClosedCalled = true
                Assertions.assertSame(expectedSession, session)
            }
        }
        messageDispatcher.registerHandler(handler)

        messageDispatcher.dispatchWebsocketSessionClosed(expectedSession)
        Assertions.assertTrue(onSessionClosedCalled)
    }

    @Test
    fun dispatchToWebsocketHandler() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())

        val expectedSession = object : Session() {
            override fun sendObjectSync(objectToSend: ServerInteraction) {}
            override fun sendObjectAsync(objectToSend: ServerInteraction) {}
        }

        var handlerCalled = false
        val handler = object : RequestHandlerWithWebsocketSupport<DummyRequest>() {
            override val requiresSocket = false
            override fun handle(session: Session, request: DummyRequest, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
                handlerCalled = true
                Assertions.assertSame(expectedSession, session)
                return DummyResponse(request.connectionId)
            }

            override fun handle(request: DummyRequest, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
                throw IllegalStateException("Handler cannot handle non-socket requests")
            }

            override fun canHandle(request: Request) = true
            override fun needsAuthentication(request: DummyRequest) = false

            override fun onSessionClosed(session: Session) {}
        }
        messageDispatcher.registerHandler(handler)

        messageDispatcher.dispatch(DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword), null, null, expectedSession)
        Assertions.assertTrue(handlerCalled)
    }
}
