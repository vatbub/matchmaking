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

/**
 * A room represents a group of players which intend to play together.
 * This class holds all the data related to a room.
 *
 * Usually, a room is created by one user using [Client.createRoom] or [Client.joinOrCreateRoom].
 * This user then becomes the host of the room and controls the rules of the game.
 * This does not mean that he is physically hosting the game data nor does it mean that clients do peer-to-peer-connections.
 * All clients (including the host) are still connected to the server.
 *
 * Once a room is created, other users can join using [Client.joinRoom] or [Client.joinOrCreateRoom].
 * At this stage, the game is not yet started and the server still waits for other players to join.
 * This is what many games call a lobby.
 *
 * Once enough clients have joined the room (The size of [connectedUsers] is above or equal to [minRoomSize]), the host of the room can start the game using [Client.startGame].
 * The server will then set [gameStarted] to `true` and will not let any more client sjoin the room.
 */
class Room(private val ownConnectionId: String, private val wrappedRoom: DataRoom) {
    /**
     * The id of this room.
     */
    val id: String
        get() = wrappedRoom.id

    /**
     * The connection id of the host of this room.
     * Use [amITheHost] to verify whether this client is the host of the game.
     *
     * The host of the room is typically the user who created the room.
     * Being the host of a room comes with special privileges.
     * Specifically, only the host is allowed to do the following things:
     * - Start the game using [Client.startGame]
     * - Update [gameState] using [Client.updateGameState]
     *
     * This effectively means that the host is the one who controls the rules of the game.
     */
    val hostUserConnectionId: String
        get() = wrappedRoom.hostUserConnectionId

    /**
     * Returns `true` if this client is the hst of the game, `false` otherwise.
     */
    val amITheHost = ownConnectionId == hostUserConnectionId

    /**
     * If specified, only clients whose user name is mentioned in this list are allowed to join.
     */
    val whitelist: List<String>?
        get() = wrappedRoom.whitelist

    /**
     * If specified, clients whose user name is mentioned in this list are not allowed to join.
     */
    val blacklist: List<String>?
        get() = wrappedRoom.blacklist

    /**
     * The minimal number of clients required to start a game. It is up to the host to verify this condition.
     */
    val minRoomSize: Int
        get() = wrappedRoom.minRoomSize

    /**
     * The maximum number of clients allowed to join the room. The server will make sure that no more clients join the room after this limit is reached.
     */
    val maxRoomSize: Int
        get() = wrappedRoom.maxRoomSize

    /**
     * List of users which are currently connected to the room.
     */
    val connectedUsers: List<User>
        get() = wrappedRoom.connectedUsers.toList()

    /**
     * Current state of the game.
     */
    val gameState: GameData
        get() = wrappedRoom.gameState

    /**
     * Specifies whether the game is running or not.
     */
    val gameStarted: Boolean
        get() = wrappedRoom.gameStarted

    /**
     * List of data packets which have been sent to the host of the room using [Client.sendDataToHost].
     * It is up to the host of the room to process the data and then update [gameState] accordingly using [Client.updateGameState].
     *
     * This list is distributed to all clients to allow clients to extrapolate changes to [gameState] before the host has updated the game state to reduce lag.
     */
    val dataToBeSentToTheHost: List<GameData>
        get() = wrappedRoom.dataToBeSentToTheHost.toList()


    /**
     * Creates a copy of this room.
     */
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
