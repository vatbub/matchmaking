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
package com.github.vatbub.matchmaking.server

import com.github.vatbub.matchmaking.common.responses.AuthorizationException
import com.github.vatbub.matchmaking.common.responses.UnknownConnectionIdException
import com.github.vatbub.matchmaking.server.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.dummies.DummyRequestHandler
import com.github.vatbub.matchmaking.server.idprovider.MemoryIdProvider
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

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

        val request = DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val response = messageDispatcher.dispatch(request, null, null)

        Assertions.assertNotNull(response)
        Assertions.assertTrue(handler.handledRequests.isNotEmpty())
        Assertions.assertSame(handler.handledRequests[request], response)
    }

    @Test
    fun negativeDispatchTest() {
        val messageDispatcher = MessageDispatcher(MemoryIdProvider())
        val handler = DummyRequestHandler()

        val request = DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
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
            DummyRequest(TestUtils.getRandomHexString(id.connectionId), TestUtils.getRandomHexString(id.password))
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
        val request = DummyRequest(id.connectionId, TestUtils.getRandomHexString(id.password))
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
}
