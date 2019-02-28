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
package com.github.vatbub.matchmaking.common.data

import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class RoomTest : KotlinTestSuperclass() {
    @Test
    fun copyTest() {
        val original =
            Room(
                TestUtils.getRandomHexString(),
                TestUtils.defaultConnectionId,
                listOf("vatbub", "heykey"),
                listOf("leoll"),
                2,
                5
            )
        original.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))
        original.gameState["someKey"] = "someValue"
        original.gameStarted = true
        val dataToHost = GameData(TestUtils.getRandomHexString(TestUtils.defaultConnectionId))
        dataToHost["anotherKey"] = "anotherValue"
        original.dataToBeSentToTheHost.add(dataToHost)

        val copy = original.copy()

        Assertions.assertEquals(original.id, copy.id)
        Assertions.assertEquals(original.hostUserConnectionId, copy.hostUserConnectionId)
        Assertions.assertEquals(original.whitelist, copy.whitelist)
        Assertions.assertEquals(original.blacklist, copy.blacklist)
        Assertions.assertEquals(original.minRoomSize, copy.minRoomSize)
        Assertions.assertEquals(original.maxRoomSize, copy.maxRoomSize)
        Assertions.assertEquals(original.connectedUsers, copy.connectedUsers)
        Assertions.assertEquals(original.gameState, copy.gameState)
        Assertions.assertEquals(original.gameStarted, copy.gameStarted)
        Assertions.assertEquals(original.dataToBeSentToTheHost, copy.dataToBeSentToTheHost)
    }

    @Test
    fun equalsTest() {
        val original = Room(
            TestUtils.getRandomHexString(),
            TestUtils.defaultConnectionId,
            listOf("vatbub", "heykey"),
            listOf("leoll"),
            2,
            5
        )

        val copy = original.copy()

        Assertions.assertEquals(original, copy)
        Assertions.assertNotSame(original, copy)
    }

    @Test
    fun hashCodeTest() {
        val original = Room(
            TestUtils.getRandomHexString(),
            TestUtils.defaultConnectionId,
            listOf("vatbub", "heykey"),
            listOf("leoll"),
            2,
            5
        )

        val copy = original.copy()

        Assertions.assertEquals(original.hashCode(), copy.hashCode())
    }
}
