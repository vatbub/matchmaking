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

import com.github.vatbub.matchmaking.server.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.dummies.DummyRequestHandler
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import org.junit.Assert
import org.junit.jupiter.api.Test

class MessageDispatcherTest : KotlinTestSuperclass() {
    @Test
    fun registerSameHandlerTwiceTest() {
        val messageDispatcher = MessageDispatcher()
        val handler = DummyRequestHandler()

        messageDispatcher.registerHandler(handler)
        messageDispatcher.registerHandler(handler)

        Assert.assertEquals(1, messageDispatcher.handlers.size)
    }

    @Test
    fun registerHandlerTest() {
        val messageDispatcher = MessageDispatcher()
        val handler = DummyRequestHandler()

        Assert.assertFalse(messageDispatcher.isHandlerRegistered(handler))
        messageDispatcher.registerHandler(handler)
        Assert.assertTrue(messageDispatcher.isHandlerRegistered(handler))
    }

    @Test
    fun positiveRemoveHandlerTest() {
        val messageDispatcher = MessageDispatcher()
        val handler = DummyRequestHandler()
        messageDispatcher.registerHandler(handler)
        Assert.assertTrue(messageDispatcher.removeHandler(handler))
        Assert.assertFalse(messageDispatcher.isHandlerRegistered(handler))
    }

    @Test
    fun negativeRemoveHandlerTest() {
        val messageDispatcher = MessageDispatcher()
        val handler = DummyRequestHandler()
        Assert.assertFalse(messageDispatcher.removeHandler(handler))
        Assert.assertFalse(messageDispatcher.isHandlerRegistered(handler))
    }

    @Test
    fun positiveDispatchTest() {
        val messageDispatcher = MessageDispatcher()
        val handler = DummyRequestHandler()
        messageDispatcher.registerHandler(handler)

        val connectionId = (4567876543).toString(16)
        val request = DummyRequest(connectionId)
        val response = messageDispatcher.dispatch(request)

        Assert.assertNotNull(response)
        Assert.assertTrue(handler.handledRequests.isNotEmpty())
        Assert.assertSame(handler.handledRequests[request], response)
    }

    @Test
    fun negativeDispatchTest() {
        val messageDispatcher = MessageDispatcher()
        val handler = DummyRequestHandler()

        val connectionId = (4567876543).toString(16)
        val request = DummyRequest(connectionId)
        val response = messageDispatcher.dispatch(request)

        Assert.assertNull(response)
        Assert.assertTrue(handler.handledRequests.isEmpty())
    }
}
