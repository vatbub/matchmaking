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
package com.github.vatbub.matchmaking.server.handlers

import com.github.vatbub.matchmaking.common.requests.GetConnectionIdRequest
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.server.dummies.DummyRequest
import com.github.vatbub.matchmaking.server.idprovider.MemoryIdProvider
import org.junit.Assert
import org.junit.jupiter.api.Test

class GetConnectionIdHandlerTest : RequestHandlerTestSuperclass() {
    @Test
    override fun positiveCanHandleTest() {
        val request = GetConnectionIdRequest()
        val handler = GetConnectionIdHandler(MemoryIdProvider())
        Assert.assertTrue(handler.canHandle(request))
    }

    @Test
    override fun negativeCanHandleTest() {
        val request = DummyRequest()
        val handler = GetConnectionIdHandler(MemoryIdProvider())
        Assert.assertFalse(handler.canHandle(request))
    }

    @Test
    override fun handleTest() {
        val idProvider = MemoryIdProvider()
        val handler = GetConnectionIdHandler(idProvider)
        val request = GetConnectionIdRequest()
        val response = handler.handle(request, null, null)

        Assert.assertTrue(response is GetConnectionIdResponse)
        Assert.assertEquals(
            idProvider.connectionIdsInUse[idProvider.connectionIdsInUse.size - 1],
            response.connectionId
        )
    }
}
