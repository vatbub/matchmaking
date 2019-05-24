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
package com.github.vatbub.matchmaking.common.serializationtests.requests

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.requests.UpdateGameStateRequest
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultConnectionId
import com.github.vatbub.matchmaking.testutils.TestUtils.defaultPassword
import com.github.vatbub.matchmaking.testutils.TestUtils.getRandomHexString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UpdateGameStateRequestSerializationTest :
        RequestSerializationTestSuperclass<UpdateGameStateRequest>(UpdateGameStateRequest::class.java) {
    override fun newObjectUnderTest(connectionId: String, password: String, requestId: String?) =
            UpdateGameStateRequest(connectionId, password, getRandomHexString(), GameData(connectionId), listOf(), requestId)

    override fun newObjectUnderTest() = newObjectUnderTest(defaultConnectionId, defaultPassword)

    @Test
    override fun notEqualsTest() {
        val request1 = newObjectUnderTest()
        val request2 = UpdateGameStateRequest(request1.connectionId!!, request1.password!!, getRandomHexString(request1.roomId), request1.gameData, request1.processedData, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    @Test
    fun gameDataNotEqualsTest() {
        val request1 = newObjectUnderTest()
        Thread.sleep(1000)
        val request2 = UpdateGameStateRequest(request1.connectionId!!, request1.password!!, request1.roomId, GameData(request1.connectionId!!), request1.processedData, request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }

    @Test
    fun processedGameDataNotEqualsTest() {
        val request1 = newObjectUnderTest()
        val request2 = UpdateGameStateRequest(request1.connectionId!!, request1.password!!, request1.roomId, request1.gameData, listOf(GameData(request1.connectionId!!)), request1.requestId)
        Assertions.assertNotEquals(request1, request2)
    }
}
