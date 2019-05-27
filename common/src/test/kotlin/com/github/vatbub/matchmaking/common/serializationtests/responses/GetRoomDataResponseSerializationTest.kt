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
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.testutils.TestUtils.getRandomHexString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GetRoomDataResponseSerializationTest :
        ResponseImplSerializationTestSuperclass<GetRoomDataResponse>(GetRoomDataResponse::class.java) {
    override fun newObjectUnderTest(connectionId: String?, responseTo: String?) =
            GetRoomDataResponse(connectionId, Room(getRandomHexString(), connectionId!!), responseTo)

    @Test
    override fun notEqualsTest() {
        val response1 = newObjectUnderTest()
        val room1 = response1.room!!
        val room2 = Room(getRandomHexString(room1.id), getRandomHexString(room1.hostUserConnectionId))
        val response2 = GetRoomDataResponse(response1.connectionId, room2, response1.responseTo)
        Assertions.assertNotEquals(response1, response2)
    }

    @Test
    fun roomNotEqualHashCodeNotEqual() {
        val instance1 = newObjectUnderTest()
        val instance2 = GetRoomDataResponse(instance1.connectionId, null, instance1.responseTo)
        Assertions.assertNotEquals(instance1.hashCode(), instance2.hashCode())
    }
}
