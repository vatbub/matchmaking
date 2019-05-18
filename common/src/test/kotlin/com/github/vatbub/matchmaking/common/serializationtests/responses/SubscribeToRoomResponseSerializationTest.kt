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

import com.github.vatbub.matchmaking.common.responses.SubscribeToRoomResponse
import com.github.vatbub.matchmaking.common.serializationtests.ServerInteractionSerializationTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class SubscribeToRoomResponseSerializationTest :
        ServerInteractionSerializationTestSuperclass<SubscribeToRoomResponse>(SubscribeToRoomResponse::class.java) {
    override fun newObjectUnderTest(): SubscribeToRoomResponse {
        return SubscribeToRoomResponse(TestUtils.defaultConnectionId)
    }

    @Test
    override fun notEqualsTest() {
        val response1 = newObjectUnderTest()
        val response2 = SubscribeToRoomResponse(TestUtils.getRandomHexString(response1.connectionId), response1.responseTo)
        Assertions.assertNotEquals(response1, response2)
    }
}
