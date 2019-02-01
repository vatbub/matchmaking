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
package com.github.vatbub.matchmaking.server.roomproviders

import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.requests.UserListMode
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclass
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class RoomProviderTest(private val roomProvider: RoomProvider) : KotlinTestSuperclass() {
    @BeforeEach
    fun setUp() {
        roomProvider.clearRooms()
    }

    @Test
    fun negativeContainsTest() {
        Assertions.assertFalse(roomProvider.containsRoom("khczufgijkln"))
    }

    @Test
    fun positiveContainsTest() {
        val room = roomProvider.createNewRoom("1d6aa98d")
        Assertions.assertTrue(roomProvider.containsRoom(room.id))
    }

    @Test
    fun createRoomTest() {
        val expectedRooms = listOf(
            Room("", "325f6f32", listOf("vatbub", "mo-mar"), UserListMode.Whitelist, 2, 5),
            Room("", "22321b1b", listOf("heykey", "mylord"), UserListMode.Blacklist, 4, 10),
            Room("", "0208e980", listOf("somedude", "guys"), UserListMode.Ignore, 3, 4),
            Room("", "29c806f4")
        )

        val roomIds = mutableListOf<String>()
        for (expectedRoom in expectedRooms) {
            val room = roomProvider.createNewRoom(
                expectedRoom.hostUserConnectionId,
                expectedRoom.configuredUserNameList,
                expectedRoom.configuredUserNameListMode,
                expectedRoom.minRoomSize,
                expectedRoom.maxRoomSize
            )
            Assertions.assertEquals(expectedRoom.hostUserConnectionId, room.hostUserConnectionId)
            Assertions.assertEquals(expectedRoom.configuredUserNameList, room.configuredUserNameList)
            Assertions.assertEquals(expectedRoom.configuredUserNameListMode, room.configuredUserNameListMode)
            Assertions.assertEquals(expectedRoom.minRoomSize, room.minRoomSize)
            Assertions.assertEquals(expectedRoom.maxRoomSize, room.maxRoomSize)
            Assertions.assertFalse(roomIds.contains(room.id))
            roomIds.add(room.id)
        }
    }

    @Test
    fun getRoomTest() {
        val createdRoom1 = roomProvider.createNewRoom("29c806f4")
        val createdRoom2 = roomProvider.createNewRoom("325f6f32")
        val retrievedRoom1 = roomProvider[createdRoom1.id]
        val retrievedRoom2 = roomProvider[createdRoom2.id]
        Assertions.assertEquals(createdRoom1, retrievedRoom1)
        Assertions.assertEquals(createdRoom2, retrievedRoom2)
        Assertions.assertNotEquals(createdRoom1, retrievedRoom2)
        Assertions.assertNotEquals(createdRoom2, retrievedRoom1)
    }

    @Test
    fun getRoomsByIdTest() {
        val roomsToGet = mutableListOf("21e8b855", "36f1d82b")
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )
        for (hostConnectionId in hostConnectionIds) {
            var createdRoom: Room? = null
            do {
                if (createdRoom != null)
                    roomProvider.deleteRoom(createdRoom.id)
                createdRoom = roomProvider.createNewRoom(hostConnectionId)
            } while (createdRoom == null || roomsToGet.contains(createdRoom.id))

            roomsToGet.add(createdRoom.id)
        }

        val retrievedRooms = roomProvider.getRoomsById(roomsToGet)
        Assertions.assertEquals(hostConnectionIds.size, retrievedRooms.size)

        for (room in retrievedRooms) {
            Assertions.assertTrue(hostConnectionIds.contains(room.hostUserConnectionId))
        }
    }

    @Test
    fun deleteRoomTest() {
        val room = roomProvider.createNewRoom("1ffbec47")
        Assertions.assertTrue(roomProvider.containsRoom(room.id))
        Assertions.assertNotNull(roomProvider[room.id])

        Assertions.assertEquals(room, roomProvider.deleteRoom(room.id))

        Assertions.assertFalse(roomProvider.containsRoom(room.id))
        Assertions.assertNull(roomProvider[room.id])
    }

    @Test
    fun deleteRoomsTest() {
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )
        val rooms = mutableListOf<Room>()
        val roomIds = mutableListOf<String>()

        for (hostConnectionId in hostConnectionIds) {
            val room = roomProvider.createNewRoom(hostConnectionId)
            rooms.add(room)
            roomIds.add(room.id)
        }

        for (room in rooms) {
            Assertions.assertTrue(roomProvider.containsRoom(room.id))
            Assertions.assertNotNull(roomProvider[room.id])
        }

        val deletedRooms = roomProvider.deleteRooms(*roomIds.toTypedArray())

        Assertions.assertEquals(rooms, deletedRooms)

        for (room in rooms) {
            Assertions.assertFalse(roomProvider.containsRoom(room.id))
            Assertions.assertNull(roomProvider[room.id])
        }
    }

    @Test
    fun clearRoomsTest() {
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )

        for (hostConnectionId in hostConnectionIds) {
            roomProvider.createNewRoom(hostConnectionId)
        }

        Assertions.assertEquals(hostConnectionIds.size, roomProvider.getAllRooms().size)

        roomProvider.clearRooms()

        Assertions.assertEquals(0, roomProvider.getAllRooms().size)
    }

    @Test
    fun getAllRoomsTest() {
        val hostConnectionIds = listOf(
            "250b7528",
            "2ac2ed78",
            "2d4d21d8",
            "19af35dc",
            "10a032a5",
            "0cc14cbe",
            "351a4d9a",
            "16567c41",
            "0d9d3410",
            "32f5e17c"
        )
        val createdRooms = mutableListOf<Room>()

        for (hostConnectionId in hostConnectionIds) {
            createdRooms.add(roomProvider.createNewRoom(hostConnectionId))
        }

        val allRooms = roomProvider.getAllRooms()

        Assertions.assertEquals(createdRooms.size, allRooms.size)

        for (room in allRooms) {
            Assertions.assertTrue(createdRooms.contains(room))
        }
    }
}
