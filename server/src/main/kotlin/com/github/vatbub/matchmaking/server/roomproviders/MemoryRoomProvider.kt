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
import com.github.vatbub.matchmaking.server.roomproviders.data.ObservableRoom
import com.github.vatbub.matchmaking.server.roomproviders.data.RoomTransaction
import kotlin.random.Random

/**
 * This implementation of [RoomProvider] keeps the provided rooms in memory. This implementation therefore
 * - *does not* support sharing data across multiple nodes
 * - *does not* persist its data across server restarts
 */
open class MemoryRoomProvider : RoomProvider() {
    override val supportsConcurrentTransactionsOnSameRoom = false

    private val rooms = mutableMapOf<String, Room>()
    private val pendingTransactions = mutableMapOf<String, RoomTransaction>()

    override fun beginTransactionWithRoom(id: String): RoomTransaction? {
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
        if (!pendingTransactions.containsValue(roomTransaction)) return
        rooms[roomTransaction.room.id] = roomTransaction.room.toRoom()
        pendingTransactions.remove(roomTransaction.room.id)
    }

    override fun abortTransaction(roomTransaction: RoomTransaction) {
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

    override fun get(id: String): Room? {
        return rooms[id]?.copy()
    }

    override fun deleteRoom(id: String): Room? {
        return rooms.remove(id)
    }

    override fun clearRooms() {
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
}
