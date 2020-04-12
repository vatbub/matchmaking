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
import com.github.vatbub.matchmaking.server.logic.roomproviders.data.ObservableRoom
import com.github.vatbub.matchmaking.server.logic.roomproviders.data.RoomTransaction
import kotlin.random.Random

/**
 * This implementation of [RoomProvider] keeps the provided rooms in memory. This implementation therefore
 * - *does not* support sharing data across multiple nodes
 * - *does not* persist its data across server restarts
 */
open class MemoryRoomProvider : RoomProvider() {
    override val supportsConcurrentTransactionsOnSameRoom = false

    internal val rooms = mutableMapOf<String, Room>()
    private val pendingTransactions = mutableMapOf<String, RoomTransaction>()

    override fun beginTransactionWithRoom(id: String): RoomTransaction? {
        logger.trace { "Beginning a transaction for room $id..." }
        val room = rooms[id] ?: return null
        val transaction = RoomTransaction(
                ObservableRoom(room), this
        )
        while (true) {
            synchronized(pendingTransactions) {
                if (!pendingTransactions.containsKey(room.id)) {
                    pendingTransactions[room.id] = transaction
                    return transaction
                }
            }
        }
    }

    override fun commitTransactionImpl(roomTransaction: RoomTransaction) {
        logger.trace { "Committing a room transaction for room ${roomTransaction.room.id}" }
        if (!pendingTransactions.containsValue(roomTransaction)) return
        rooms[roomTransaction.room.id] = roomTransaction.room.toRoom()
        pendingTransactions.remove(roomTransaction.room.id)
    }

    override fun abortTransaction(roomTransaction: RoomTransaction) {
        logger.trace { "Aborting a room transaction for room ${roomTransaction.room.id}" }
        pendingTransactions.remove(roomTransaction.room.id)
    }

    override fun getAllRooms(): Collection<Room> {
        return rooms.values.toList() // copy the list
    }

    override fun containsRoom(id: String): Boolean {
        return rooms.containsKey(id)
    }

    override fun createNewRoom(
            hostUserConnectionId: String,
            whitelist: List<String>?,
            blacklist: List<String>?,
            minRoomSize: Int,
            maxRoomSize: Int
    ): Room {
        logger.trace { "Creating a new room..." }
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
                whitelist,
                blacklist,
                minRoomSize,
                maxRoomSize
        )
        rooms[roomIdAsString] = room
        return room
    }

    override fun get(id: String) = rooms[id]?.copy()

    override fun deleteRoom(id: String): Room? {
        logger.trace { "Deleting room with id $id..." }
        return rooms.remove(id)
    }

    override fun clearRooms() {
        logger.debug { "Deleting all rooms from memory..." }
        rooms.clear()
    }

    override fun forEach(action: (room: Room) -> Unit) = rooms.forEach { _, room -> action(room) }

    override fun forEachTransaction(action: (transaction: RoomTransaction) -> Unit) =
            forEach { room -> action(beginTransactionWithRoom(room.id)!!) }

    override fun filter(filter: (room: Room) -> Boolean): Collection<Room> {
        val result = mutableListOf<Room>()
        rooms.filter { filter(it.value) }.forEach {
            result.add(it.value.copy())
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MemoryRoomProvider

        if (supportsConcurrentTransactionsOnSameRoom != other.supportsConcurrentTransactionsOnSameRoom) return false
        if (rooms != other.rooms) return false
        if (pendingTransactions != other.pendingTransactions) return false

        return true
    }

    override fun hashCode(): Int {
        var result = supportsConcurrentTransactionsOnSameRoom.hashCode()
        result = 31 * result + rooms.hashCode()
        result = 31 * result + pendingTransactions.hashCode()
        return result
    }
}
