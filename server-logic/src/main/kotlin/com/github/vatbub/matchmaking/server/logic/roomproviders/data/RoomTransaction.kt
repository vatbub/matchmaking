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

import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider

/**
 * [RoomTransaction]s are used to perform changes on rooms. The reason for the transaction principle is to ensure thread safety.
 * Multiple threads and/or multiple nodes might access the [RoomProvider] at the same time and they all need a consistent
 * database state.
 * Therefore, all changes to rooms must be done within one transaction. The changes will not become visible to other threads until
 * [RoomTransaction.commit] is called.
 */
class RoomTransaction(room: ObservableRoom, internal val roomProvider: RoomProvider) {
    companion object {
        private val idsInUse = mutableListOf<Int>()
        private val nextId: Int
            get() {
                synchronized(idsInUse) {
                    val id = (idsInUse.max() ?: 0) + 1
                    idsInUse.add(id)
                    return id
                }
            }
    }

    val id = nextId
    private val _room = room
    val room: ObservableRoom
        get() {
            if (finalized)
                throw IllegalStateException("A RoomTransaction cannot be modified after it has been committed or aborted.")
            return _room
        }
    var finalized = false
        private set

    /**
     * Changes to the room will only be visible after this method has been called.
     * Changes to the transaction will result in an [IllegalStateException] after calling this method.
     */
    fun commit() {
        if (finalized) return
        roomProvider.commitTransaction(this)
        finalized = true
    }

    /**
     * Cancels all changes performed by this transaction.
     * Changes to the transaction will result in an [IllegalStateException] after calling this method.
     */
    fun abort() {
        if (finalized) return
        roomProvider.abortTransaction(this)
        finalized = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomTransaction

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

}
