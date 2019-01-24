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
import com.github.vatbub.matchmaking.common.requests.UserListMode.*

abstract class RoomProvider {
    /**
     * Creates a new room with the specified parameters, stores it and returns it.
     * @param hostUserConnectionId The connection id of the game host
     * @param configuredUserNameList The user name list to be used in the room
     * @param configuredUserNameListMode The [UserListMode] of [configuredUserNameList]
     * @param minRoomSize The minimum amount of players required to start a game in that room
     * @param maxRoomSize The maximum amount of players required to start a game in that room
     * @return The new [Room]
     */
    abstract fun createNewRoom(
        hostUserConnectionId: String,
        configuredUserNameList: List<String>? = null,
        configuredUserNameListMode: UserListMode = Ignore,
        minRoomSize: Int = 1,
        maxRoomSize: Int = 1
    ): Room

    /**
     * Returns the [Room] with the specified id or `null` if no room with the given id was found.
     * **IMPORTANT:** Depending on the implementation, changes to the [Room] object might not be saved automatically.
     * To make sure that changes are saved, call [commitChangesToRoom]
     * @param id The id of the room to get
     * @return The [Room] with the specified id or `null` if no room with the given id was found
     * @see commitChangesToRoom
     */
    abstract operator fun get(id: String): Room?

    /**
     * Makes sure that changes to the supplied rooms are saved in the room provider
     */
    abstract fun commitChangesToRoom(vararg roomsToCommit: Room)

    open fun getRoomsById(ids: Collection<String>): List<Room> {
        return getRoomsById(*ids.toTypedArray())
    }

    open fun getRoomsById(vararg ids: String): List<Room> {
        val result = mutableListOf<Room>()
        for (room in getAllRooms()) {
            if (ids.contains(room.id))
                result.add(room)
        }
        return result
    }

    /**
     * Checks whether a room exists where the user can join into.
     * @param userName The name of the user to join. This user name should not be on a room's blacklist
     * (if the room has a blacklist) and must be on the room's whitelist (if the room has a whitelist).
     * @param userList A whitelist or blacklist of users that the user wishes to play with. If not null and
     * `[userListMode] != [UserListMode.Ignore]`, this list must match the room's list.
     * @param minRoomSize The minimum size of the room that the user wishes to play in. The minimum size of a room
     * must be equal or higher than this value.
     * @param maxRoomSize The maximum size of the room that the user wishes to play in. The maximum size of a room
     * must be equal or lower than this value.
     * @return The room that was found to be applicable
     */
    open fun hasApplicableRoom(
        userName: String,
        userList: List<String>? = null,
        userListMode: UserListMode = Ignore,
        minRoomSize: Int = 1,
        maxRoomSize: Int = 1
    ): Room? {
        for (room in getAllRooms()) {
            if (room.gameStarted) continue
            if ((room.connectedUsers.size + 1) > room.maxRoomSize) continue
            if (room.minRoomSize < minRoomSize) continue
            if (room.maxRoomSize > maxRoomSize) continue

            // check the supplied user list
            when (userListMode) {
                Blacklist -> {
                    if (userList == null)
                        throw IllegalArgumentException("UserList must not be null when using UserListMode.Blacklist")
                    for (user in room.connectedUsers)
                        if (userList.contains(user.userName))
                            continue
                }
                Whitelist -> {
                    if (userList == null)
                        throw IllegalArgumentException("UserList must not be null when using UserListMode.Whitelist")
                    for (user in room.connectedUsers)
                        if (!userList.contains(user.userName))
                            continue
                }
                Ignore ->
                    if (userList != null)
                        throw IllegalArgumentException("UserList must be null when using UserListMode.Ignore")
            }

            // check the room's user list
            val configuredUserNameList = room.configuredUserNameList
            val configuredUserNameListMode = room.configuredUserNameListMode

            if (configuredUserNameList != null && configuredUserNameListMode != Ignore) {
                @Suppress("NON_EXHAUSTIVE_WHEN")
                when (configuredUserNameListMode) {
                    Blacklist ->
                        for (user in configuredUserNameList)
                            if (configuredUserNameList.contains(userName))
                                continue
                    Whitelist ->
                        for (user in configuredUserNameList)
                            if (!configuredUserNameList.contains(userName))
                                continue
                }
            }

            return room
        }

        // nothing found
        return null
    }

    /**
     * Deletes the room with the specified id if the id exists.
     * @param id The id of the room to delete
     * @return The deleted room or `null` if the id was not found.
     */
    abstract fun deleteRoom(id: String): Room?

    /**
     * Deletes the rooms with the specified ids if the ids exists.
     * @param ids The ids of the rooms to delete
     * @return A list of deleted rooms
     */
    open fun deleteRooms(vararg ids: String): List<Room> {
        val deletedRooms = mutableListOf<Room>()
        for (id in ids) {
            val deletedRoom = deleteRoom(id)
            if (deletedRoom != null)
                deletedRooms.add(deletedRoom)
        }
        return deletedRooms
    }

    /**
     * Deletes all rooms
     */
    abstract fun clearRooms()

    /**
     * Checks whether a room with the given id exists.
     */
    abstract fun containsRoom(id: String): Boolean

    abstract fun getAllRooms(): Collection<Room>
}
