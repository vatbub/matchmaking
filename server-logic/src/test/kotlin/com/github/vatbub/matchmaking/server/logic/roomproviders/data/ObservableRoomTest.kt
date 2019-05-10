/*-
 * #%L
 * matchmaking.server
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
package com.github.vatbub.matchmaking.server.logic.roomproviders.data

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ObservableRoomTest : KotlinTestSuperclass<ObservableRoom>() {
    override fun newObjectUnderTest() = ObservableRoom(Room(TestUtils.getRandomHexString(), TestUtils.defaultConnectionId))

    @Test
    fun copyTest() {
        val room =
                Room(
                        TestUtils.getRandomHexString(),
                        TestUtils.defaultConnectionId,
                        listOf("vatbub", "heykey"),
                        listOf("leoll"),
                        2,
                        5
                )
        room.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))
        room.gameState["someKey"] = "someValue"
        room.gameStarted = true
        val dataToHost = GameData(room.hostUserConnectionId)
        dataToHost["anotherKey"] = "anotherValue"
        room.dataToBeSentToTheHost.add(dataToHost)

        val observableRoom = ObservableRoom(room)

        Assertions.assertEquals(room.id, observableRoom.id)
        Assertions.assertEquals(room.hostUserConnectionId, observableRoom.hostUserConnectionId)
        Assertions.assertEquals(room.whitelist, observableRoom.whitelist)
        Assertions.assertEquals(room.blacklist, observableRoom.blacklist)
        Assertions.assertEquals(room.minRoomSize, observableRoom.minRoomSize)
        Assertions.assertEquals(room.maxRoomSize, observableRoom.maxRoomSize)
        Assertions.assertEquals(room.connectedUsers, observableRoom.connectedUsers)
        Assertions.assertEquals(ObservableGameData(room.gameState), observableRoom.gameState)
        Assertions.assertEquals(room.gameStarted, observableRoom.gameStarted)
        Assertions.assertEquals(room.dataToBeSentToTheHost, observableRoom.dataToBeSentToTheHost)
    }

    @Test
    fun toRoomTest() {
        val room =
                Room(
                        TestUtils.getRandomHexString(),
                        TestUtils.defaultConnectionId,
                        listOf("vatbub", "heykey"),
                        listOf("leoll"),
                        2,
                        5
                )
        room.connectedUsers.add(User(TestUtils.defaultConnectionId, "vatbub"))
        room.gameState["someKey"] = "someValue"
        room.gameStarted = true
        val dataToHost = GameData(room.hostUserConnectionId)
        dataToHost["anotherKey"] = "anotherValue"
        room.dataToBeSentToTheHost.add(dataToHost)

        val observableRoom = ObservableRoom(room)

        Assertions.assertEquals(room, observableRoom.toRoom())
        Assertions.assertNotSame(room, observableRoom.toRoom())
    }

    @Test
    fun onGameStartedChangeTest() {
        val room = Room(TestUtils.getRandomHexString(), TestUtils.defaultConnectionId)

        val expectedNewValue = true
        var listenerCalled = false
        val observableRoom = ObservableRoom(room) { newValue ->
            Assertions.assertEquals(expectedNewValue, newValue)
            listenerCalled = true
        }

        observableRoom.gameStarted = true
        Assertions.assertTrue(listenerCalled)
    }
}
