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
package com.github.vatbub.matchmaking.server.logic.roomproviders

import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.server.logic.roomproviders.data.RoomTransaction

abstract class RoomProvider {
    abstract val supportsConcurrentTransactionsOnSameRoom: Boolean

    val onCommitRoomTransactionListeners = mutableListOf<OnCommitRoomTransactionListener>()

    /**
     * Creates a new room with the specified parameters, stores it and returns it.
     * @param hostUserConnectionId The connection id of the game host
     * @param whitelist If specified, a list of users allowed to join this room
     * @param blacklist If specified, a list of users prohibited to join this room
     * @param minRoomSize The minimum amount of players required to start a game in that room
     * @param maxRoomSize The maximum amount of players required to start a game in that room
     * @return The new [Room]
     */
    abstract fun createNewRoom(
            hostUserConnectionId: String,
            whitelist: List<String>? = null,
            blacklist: List<String>? = null,
            minRoomSize: Int = 1,
            maxRoomSize: Int = 2
    ): Room

    /**
     * Returns the [Room] with the specified id or `null` if no room with the given id was found.
     * **IMPORTANT:** Changes to the returned object will not be saved automatically. To be able to
     * make changes to a room, use [beginTransactionWithRoom], do your changes and then call
     * [RoomTransaction.commit] to save your changes.
     * @param id The id of the room to get
     * @return The [Room] with the specified id or `null` if no room with the given id was found
     * @see beginTransactionWithRoom
     */
    abstract operator fun get(id: String): Room?

    abstract fun beginTransactionWithRoom(id: String): RoomTransaction?

    fun beginTransactionsWithRooms(
            onTransactionAvailable: ((roomTransaction: RoomTransaction) -> Unit),
            ids: Collection<String>
    ) {
        beginTransactionsWithRooms(onTransactionAvailable, *ids.toTypedArray())
    }

    fun beginTransactionsWithRooms(
            onTransactionAvailable: ((roomTransaction: RoomTransaction) -> Unit),
            vararg ids: String
    ) {
        beginTransactionsForRoomsWithFilter({ ids.contains(it.id) }, onTransactionAvailable)
    }

    fun beginTransactionsForRoomsWithFilter(
            filter: ((Room) -> Boolean),
            onTransactionAvailable: ((roomTransaction: RoomTransaction) -> Unit)
    ) {
        forEachTransaction { transaction ->
            if (filter(transaction.room.toRoom()))
                onTransactionAvailable(transaction)
            else
                transaction.abort()
        }
    }

    fun beginTransactionForAllRooms(onTransactionAvailable: ((roomTransaction: RoomTransaction) -> Unit)) =
            beginTransactionsForRoomsWithFilter({ true }, onTransactionAvailable)

    /**
     * Makes sure that changes to the supplied rooms are saved in the room provider
     */
    internal fun commitTransaction(roomTransaction: RoomTransaction) {
        onCommitRoomTransactionListeners.forEach { it.onCommit(roomTransaction.room.toRoom()) }
        commitTransactionImpl(roomTransaction)
    }

    internal abstract fun commitTransactionImpl(roomTransaction: RoomTransaction)

    internal abstract fun abortTransaction(roomTransaction: RoomTransaction)

    /**
     * Returns multiple rooms by their id.
     * **IMPORTANT:** Changes to the returned rooms will not be saved automatically. To be able to
     * make changes to a room, use [beginTransactionsWithRooms], do your changes and then call
     * [RoomTransaction.commit] to save your changes.
     */
    open fun getRoomsById(ids: Collection<String>) = getRoomsById(*ids.toTypedArray())

    /**
     * Returns multiple rooms by their id.
     * **IMPORTANT:** Changes to the returned rooms will not be saved automatically. To be able to
     * make changes to a room, use [beginTransactionsWithRooms], do your changes and then call
     * [RoomTransaction.commit] to save your changes.
     */
    open fun getRoomsById(vararg ids: String) = filter { ids.contains(it.id) }

    /**
     * Checks whether a room exists where the user can join into.
     * @param userName The name of the user to join. This user name should not be on a room's blacklist
     * (if the room has a blacklist) and must be on the room's whitelist (if the room has a whitelist).
     * @param whitelist A whitelist of users that the user wishes to play with. If not null, this list must match the room's list.
     * @param blacklist A blacklist of users that the user wishes to play with. If not null, this list must match the room's list.
     * @param minRoomSize The minimum size of the room that the user wishes to play in. The minimum size of a room
     * must be equal or higher than this value.
     * @param maxRoomSize The maximum size of the room that the user wishes to play in. The maximum size of a room
     * must be equal or lower than this value.
     * @return The room that was found to be applicable
     */
    open fun hasApplicableRoom(
            userName: String,
            whitelist: List<String>? = null,
            blacklist: List<String>? = null,
            minRoomSize: Int = 1,
            maxRoomSize: Int = 2
    ): RoomTransaction? {
        logger.debug { "Checking for applicable rooms..." }
        logger.trace { "hasApplicableRoom parameters: userName = $userName, whitelist = $whitelist, blacklist = $blacklist, minRoomSize = $minRoomSize, maxRoomSize = $maxRoomSize" }
        var result: RoomTransaction? = null

        beginTransactionsForRoomsWithFilter({ room -> !room.gameStarted && result == null }, { roomTransaction ->
            if (roomTransaction.canUserJoinOrAbort(userName, whitelist, blacklist, minRoomSize, maxRoomSize))
                result = roomTransaction
        })

        logger.trace { "hasApplicableRoom result: $result" }
        return result
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

    open fun deleteRooms(filter: (Room) -> Boolean): List<Room> {
        val deletedRooms = mutableListOf<Room>()
        getAllRooms().forEach {
            if (!filter(it)) return@forEach
            deleteRoom(it.id)
            deletedRooms.add(it)
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

    /**
     * Returns all rooms known to this [RoomProvider].
     * **IMPORTANT:** Changes to the returned rooms will not be saved automatically. To be able to
     * make changes to a room, use [beginTransactionsWithRooms], do your changes and then call
     * [RoomTransaction.commit] to save your changes.
     */
    abstract fun getAllRooms(): Collection<Room>

    abstract fun forEach(action: ((room: Room) -> Unit))

    abstract fun forEachTransaction(action: ((transaction: RoomTransaction) -> Unit))

    abstract fun filter(filter: ((room: Room) -> Boolean)): Collection<Room>
}

interface OnCommitRoomTransactionListener {
    fun onCommit(room: Room)
}
