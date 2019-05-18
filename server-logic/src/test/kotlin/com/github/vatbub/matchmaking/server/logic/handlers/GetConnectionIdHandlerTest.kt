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
package com.github.vatbub.matchmaking.server.logic.handlers

import com.github.vatbub.matchmaking.common.requests.GetConnectionIdRequest
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.logic.idprovider.Id
import com.github.vatbub.matchmaking.server.logic.idprovider.MemoryIdProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GetConnectionIdHandlerTest : RequestHandlerTestSuperclass<GetConnectionIdHandler>() {
    override fun getCloneOf(instance: GetConnectionIdHandler) = GetConnectionIdHandler(instance.connectionIdProvider)
    override fun newObjectUnderTest() = GetConnectionIdHandler(MemoryIdProvider())

    @Test
    override fun needsAuthenticationTest() {
        val request = GetConnectionIdRequest()
        val handler = GetConnectionIdHandler(MemoryIdProvider())
        Assertions.assertFalse(handler.needsAuthentication(request))
    }

    @Test
    override fun positiveCanHandleTest() {
        val request = GetConnectionIdRequest()
        val handler = GetConnectionIdHandler(MemoryIdProvider())
        Assertions.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val request = DummyRequest()
        val handler = GetConnectionIdHandler(MemoryIdProvider())
        Assertions.assertFalse(handler.canHandle(request))
    }

    @Test
    override fun handleTest() {
        val idProvider = MemoryIdProvider()
        val handler = GetConnectionIdHandler(idProvider)
        val request = GetConnectionIdRequest()
        val response = handler.handle(request, null, null)

        Assertions.assertTrue(response is GetConnectionIdResponse)
        response as GetConnectionIdResponse
        Assertions.assertTrue(idProvider.connectionIdsInUse.containsKey(response.connectionId))
        Assertions.assertTrue(idProvider.connectionIdsInUse.containsValue(Id(response.connectionId, response.password)))
    }

    @Test
    override fun notEqualsTest() {
        val idProvider1 = MemoryIdProvider()
        val idProvider2 = MemoryIdProvider()
        idProvider2.getNewId()
        val handler1 = GetConnectionIdHandler(idProvider1)
        val handler2 = GetConnectionIdHandler(idProvider2)
        Assertions.assertNotEquals(handler1, handler2)
    }
}
