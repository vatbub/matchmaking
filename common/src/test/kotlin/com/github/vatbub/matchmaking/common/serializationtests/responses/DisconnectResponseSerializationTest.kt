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
package com.github.vatbub.matchmaking.common.serializationtests.responses

import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.common.serializationtests.ServerInteractionSerializationTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultConnectionId
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class DisconnectResponseSerializationTest :
        ServerInteractionSerializationTestSuperclass<DisconnectResponse>(DisconnectResponse::class.java) {
    override fun newObjectUnderTest(): DisconnectResponse {
        return DisconnectResponse(defaultConnectionId, listOf(), listOf())
    }

    @Test
    override fun notEqualsTest() {
        val response1 = newObjectUnderTest()
        val room = Room(TestUtils.getRandomHexString(), response1.connectionId!!)
        val disconnectedRooms = response1.disconnectedRooms.toMutableList()
        val destroyedRooms = response1.destroyedRooms.toMutableList()
        disconnectedRooms.add(room)
        destroyedRooms.add(room)
        val response2 = DisconnectResponse(response1.connectionId, disconnectedRooms, destroyedRooms)
        Assertions.assertNotEquals(response1, response2)
    }
}
