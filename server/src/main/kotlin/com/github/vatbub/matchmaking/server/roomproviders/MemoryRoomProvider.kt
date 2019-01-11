package com.github.vatbub.matchmaking.server.roomproviders

import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.requests.UserListMode
import kotlin.random.Random

class MemoryRoomProvider : RoomProvider() {
    override fun getAllRooms(): Collection<Room> {
        return rooms.values
    }

    override fun containsRoom(id: String): Boolean {
        return rooms.containsKey(id)
    }

    override fun createNewRoom(
        hostUserConnectionId: String,
        configuredUserNameList: List<String>?,
        configuredUserNameListMode: UserListMode,
        minRoomSize: Int,
        maxRoomSize: Int
    ): Room {

        var roomIdAsString: String
        do {
            var roomId = Random.nextInt()
            if (roomId < 0)
                roomId = -roomId

            roomIdAsString = roomId.toString(16)
        } while (containsRoom(roomIdAsString))

        val room = Room(
            roomIdAsString,
            hostUserConnectionId,
            configuredUserNameList,
            configuredUserNameListMode,
            minRoomSize,
            maxRoomSize
        )
        rooms[roomIdAsString] = room
        return room
    }

    override fun get(id: String): Room? {
        return rooms[id]
    }

    override fun deleteRoom(id: String): Room? {
        return rooms.remove(id)
    }

    override fun deleteRooms(vararg ids: String): List<Room> {
        val deletedRooms = mutableListOf<Room>()
        for (id in ids) {
            val deletedRoom = rooms.remove(id)
            if (deletedRoom != null)
                deletedRooms.add(deletedRoom)
        }
        return deletedRooms
    }

    override fun clearRooms() {
        rooms.clear()
    }

    private val rooms = mutableMapOf<String, Room>()
}