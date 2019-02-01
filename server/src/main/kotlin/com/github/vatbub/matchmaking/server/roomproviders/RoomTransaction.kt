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

class RoomTransaction(val room: Room, private val roomProvider: RoomProvider) {
    var finalized = false
        private set

    fun commit() {
        if (finalized) return
        roomProvider.commitTransaction(this)
        finalized = true
    }

    fun abort() {
        if (finalized) return
        roomProvider.abortTransaction(this)
        finalized = true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomTransaction

        if (room != other.room) return false
        if (roomProvider != other.roomProvider) return false

        return true
    }

    override fun hashCode(): Int {
        var result = room.hashCode()
        result = 31 * result + roomProvider.hashCode()
        return result
    }
}
