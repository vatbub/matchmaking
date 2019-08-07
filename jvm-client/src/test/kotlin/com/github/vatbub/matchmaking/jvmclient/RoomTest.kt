/*-
 * #%L
 * matchmaking.jvm-client
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
package com.github.vatbub.matchmaking.jvmclient

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import com.github.vatbub.matchmaking.common.data.Room as DataRoom

class RoomTest : KotlinTestSuperclass<Room>() {
    private val sampleDataRoom = DataRoom(TestUtils.getRandomHexString(), TestUtils.defaultConnectionId, listOf("heykey"), listOf("mo-mar, leoll"), 5, 10)

    private fun getClientRoom(isHost: Boolean = true) =
            if (isHost)
                Room(TestUtils.defaultConnectionId, sampleDataRoom)
            else
                Room(TestUtils.getRandomHexString(TestUtils.defaultConnectionId), sampleDataRoom)

    override fun newObjectUnderTest() = getClientRoom()

    override fun getCloneOf(instance: Room) = instance.copy()

    @Test
    override fun notEqualsTest() {
        val original = Room(TestUtils.defaultConnectionId, DataRoom(
                TestUtils.getRandomHexString(),
                TestUtils.defaultConnectionId,
                listOf("vatbub", "heykey"),
                listOf("leoll"),
                2,
                5
        ))

        val copy = Room(TestUtils.defaultConnectionId, DataRoom(
                TestUtils.getRandomHexString(original.id),
                original.hostUserConnectionId,
                listOf("vatbub", "heykey"),
                listOf("leoll"),
                2,
                5
        ))

        Assertions.assertNotEquals(original, copy)
        Assertions.assertNotSame(original, copy)
    }

    @Test
    fun ownConnectionIdotEqualsTest() {
        val original = Room(TestUtils.defaultConnectionId, DataRoom(
                TestUtils.getRandomHexString(),
                TestUtils.defaultConnectionId,
                listOf("vatbub", "heykey"),
                listOf("leoll"),
                2,
                5
        ))

        val copy = Room(TestUtils.getRandomHexString(TestUtils.defaultConnectionId), DataRoom(
                original.id,
                original.hostUserConnectionId,
                listOf("vatbub", "heykey"),
                listOf("leoll"),
                2,
                5
        ))

        Assertions.assertNotEquals(original, copy)
        Assertions.assertNotSame(original, copy)
    }

    @Test
    fun idTest() {
        Assertions.assertEquals(getClientRoom().id, sampleDataRoom.id)
    }

    @Test
    fun hostUserConnectionIdTest() {
        Assertions.assertEquals(getClientRoom().hostUserConnectionId, sampleDataRoom.hostUserConnectionId)
    }

    @Test
    fun positiveAmITheHostTest() {
        Assertions.assertTrue(getClientRoom().amITheHost)
    }

    @Test
    fun negativeAmITheHostTest() {
        Assertions.assertFalse(getClientRoom(false).amITheHost)
    }

    @Test
    fun whitelistTest() {
        Assertions.assertEquals(getClientRoom().whitelist, sampleDataRoom.whitelist)
    }

    @Test
    fun blacklistTest() {
        Assertions.assertEquals(getClientRoom().blacklist, sampleDataRoom.blacklist)
    }

    @Test
    fun minRoomSizeTest() {
        Assertions.assertEquals(getClientRoom().minRoomSize, sampleDataRoom.minRoomSize)
    }

    @Test
    fun maxRoomSizeTest() {
        Assertions.assertEquals(getClientRoom().maxRoomSize, sampleDataRoom.maxRoomSize)
    }

    @Test
    fun connectedUsersTest() {
        sampleDataRoom.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))
        Assertions.assertEquals(getClientRoom().connectedUsers, sampleDataRoom.connectedUsers)
    }

    @Test
    fun gameStateTest() {
        Assertions.assertEquals(getClientRoom().gameState, sampleDataRoom.gameState)
    }

    @Test
    fun positiveGameStartedTest() {
        sampleDataRoom.gameStarted = true
        Assertions.assertTrue(getClientRoom().gameStarted)
    }

    @Test
    fun negativeGameStartedTest() {
        sampleDataRoom.gameStarted = false
        Assertions.assertFalse(getClientRoom().gameStarted)
    }

    @Test
    fun dataToBeSentToTheHostTest() {
        sampleDataRoom.dataToBeSentToTheHost.add(GameData(TestUtils.defaultConnectionId, mutableMapOf("1" to "2")))
        Assertions.assertEquals(getClientRoom().dataToBeSentToTheHost, sampleDataRoom.dataToBeSentToTheHost)
    }
}
