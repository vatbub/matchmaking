/*-
 * #%L
 * matchmaking.common
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
package com.github.vatbub.matchmaking.common.data

import com.github.vatbub.matchmaking.common.requests.StartGameRequest
import com.github.vatbub.matchmaking.common.requests.UserListMode

/**
 * Represents a room of players, also commonly called a lobby.
 * @param id The unique identifier of the room. Use this identifier for further updates to this room.
 * @param hostUserConnectionId The connectionId of the host user. This user will be responsible to check for rule violations and to set the [gameState]
 * @param configuredUserNameList The list of user names that was specified when the room was created. Either a black- or a whitelist (if not ignored)
 * @param configuredUserNameListMode The mode of [configuredUserNameList]
 * @param minRoomSize The minimum amount of players required for a game. Important: It is up to the game host to verify whether the current amount of connected users lies within the boundaries. If so, the host must start the game by sending a [StartGameRequest]
 * @param maxRoomSize The maximum amount of players allowed in the room. The server will not assign more than this number of people to this room.
 */
class Room(
    val id: String,
    val hostUserConnectionId: String,
    val configuredUserNameList: List<String>? = null,
    val configuredUserNameListMode: UserListMode = UserListMode.Ignore,
    val minRoomSize: Int = 1,
    val maxRoomSize: Int = 1
) {
    /**
     * The list of currently connected users
     */
    val connectedUsers = mutableListOf<User>()
    /**
     * The current state of the game as sent by the host. Important: It is up to the host to set this value
     */
    var gameState = GameData()
    /**
     * Specifies whether this room is currently in a game or waiting for a game to start. Important: It is up to the host to set this value
     */
    var gameStarted: Boolean = false
    /**
     * A queue of data that clients wish to send to the host. The host should process this data and update the [gameStarted] accordingly.
     * Important: The matchmaking server assumes that once the host has received the data he stores and processes it on its end.
     * The matchmaking server therefore clears the queue on his end once the queue has been sent to the user.
     */
    val dataToBeSentToTheHost = mutableListOf<GameData>()

    fun copy(): Room {
        synchronized(this) {
            val result = Room(
                id,
                hostUserConnectionId,
                configuredUserNameList,
                configuredUserNameListMode,
                minRoomSize,
                maxRoomSize
            )
            result.connectedUsers.addAll(connectedUsers)
            result.gameState = gameState.copy()
            result.gameStarted = gameStarted
            result.dataToBeSentToTheHost.addAll(dataToBeSentToTheHost)
            return result
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Room

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
