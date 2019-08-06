/*-
 * #%L
 * matchmaking.jvm-client
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
package com.github.vatbub.matchmaking.jvmclient

import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.data.Room as DataRoom

class Room(private val ownConnectionId: String, private val wrappedRoom: DataRoom) {
    val id: String
        get() = wrappedRoom.id
    val hostUserConnectionId: String
        get() = wrappedRoom.hostUserConnectionId
    val amITheHost = ownConnectionId == hostUserConnectionId
    val whitelist: List<String>?
        get() = wrappedRoom.whitelist
    val blacklist: List<String>?
        get() = wrappedRoom.blacklist
    val minRoomSize: Int
        get() = wrappedRoom.minRoomSize
    val maxRoomSize: Int
        get() = wrappedRoom.maxRoomSize
    val connectedUsers: List<User>
        get() = wrappedRoom.connectedUsers.toList()
    val gameState: GameData
        get() = wrappedRoom.gameState
    val gameStarted: Boolean
        get() = wrappedRoom.gameStarted
    val dataToBeSentToTheHost: List<GameData>
        get() = wrappedRoom.dataToBeSentToTheHost.toList()


    fun copy(ownConnectionId: String = this.ownConnectionId, wrappedRoom: DataRoom = this.wrappedRoom.copy()) =
            Room(ownConnectionId, wrappedRoom)

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (ownConnectionId != other.ownConnectionId) return false
        if (wrappedRoom != other.wrappedRoom) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ownConnectionId.hashCode()
        result = 31 * result + wrappedRoom.hashCode()
        return result
    }
}
