package com.github.vatbub.matchmaking.server.roomproviders

import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.data.User
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
    fun commitUnknownRoomTest() {
        val unknownRoom = Room("3aa02661", "22321b1b")
        roomProvider.commitChangesToRoom(unknownRoom)
        Assertions.assertFalse(roomProvider.containsRoom(unknownRoom.id))
    }

    @Test
    fun commitRoomTest() {
        val room = roomProvider.createNewRoom("2e1eb54a")
        val connectingUser = User("2d4e4630", "vatbub")
        room.connectedUsers.add(connectingUser)
        roomProvider.commitChangesToRoom(room)
        Assertions.assertEquals(room.connectedUsers, roomProvider[room.id]?.connectedUsers)
    }

    @Test
    fun commitMultipleRoomsTest() {
        val room1 = roomProvider.createNewRoom("2e1eb54a")
        val room2 = roomProvider.createNewRoom("10391622")
        val connectingUser1 = User("2d4e4630", "vatbub")
        val connectingUser2 = User("2c14467f", "heykey")

        room1.connectedUsers.add(connectingUser1)
        room2.connectedUsers.add(connectingUser2)

        roomProvider.commitChangesToRoom(room1, room2)

        Assertions.assertEquals(room1.connectedUsers, roomProvider[room1.id]?.connectedUsers)
        Assertions.assertEquals(room2.connectedUsers, roomProvider[room2.id]?.connectedUsers)
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