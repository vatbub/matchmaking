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
import kotlin.random.Random

class MemoryRoomProvider : RoomProvider() {
    override fun commitChangesToRoom(vararg roomsToCommit: Room) {
        for (room in roomsToCommit) {
            if (!containsRoom(room.id)) continue
            rooms[room.id] = room
        }
    }

    private val rooms = mutableMapOf<String, Room>()

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

    override fun clearRooms() {
        rooms.clear()
    }
}
