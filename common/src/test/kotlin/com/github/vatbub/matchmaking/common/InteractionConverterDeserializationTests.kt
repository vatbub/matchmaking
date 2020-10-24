/*-
 * #%L
 * matchmaking.common
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
package com.github.vatbub.matchmaking.common

import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultConnectionId
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultPassword
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class InteractionConverterDeserializationTests {
    @Test
    fun requestCastTest() {
        val request = DummyRequest(defaultConnectionId, defaultPassword)
        val serializedRequest = InteractionConverter.serialize(request)
        var deserializedRequest = DummyRequest()
        Assertions.assertDoesNotThrow {
            deserializedRequest = InteractionConverter.deserialize(serializedRequest)
        }
        Assertions.assertEquals(request, deserializedRequest)
    }

    @Test
    fun responseCastTest() {
        val response = DummyResponse(defaultConnectionId)
        val serializedResponse = InteractionConverter.serialize(response)
        var deserializedResponse = DummyResponse(null)
        Assertions.assertDoesNotThrow {
            deserializedResponse = InteractionConverter.deserialize(serializedResponse)
        }
        Assertions.assertEquals(response, deserializedResponse)
    }

    @Test
    fun illegalJsonTest() {
        val illegalJson = "{ljhbljhbkh"
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            InteractionConverter.deserialize<DummyResponse>(illegalJson)
        }
    }

    @Test
    fun classNameNotAStringTest() {
        val json = "{\"protocolVersion\":\"2.0\"," +
                "\"httpStatusCode\":200," +
                "\"connectionId\":\"7e189535\"," +
                "\"className\":{" +
                "\"dummyValue\":200" +
                "}" +
                "}"
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            InteractionConverter.deserialize<DummyResponse>(json)
        }
    }
}
