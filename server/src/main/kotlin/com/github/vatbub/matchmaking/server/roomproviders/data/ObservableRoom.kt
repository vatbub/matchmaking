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
package com.github.vatbub.matchmaking.server.roomproviders.data

import com.github.vatbub.matchmaking.common.data.Room
import kotlin.properties.Delegates

/**
 * This class wraps a [Room] and allows other entities to subscribe to changes to the room.
 * This allows more efficient database interactions.
 * **Important:** The room data is copied within the constructor. Later changes to `fromRoom` will not be reflected by this class.
 * @param fromRoom The room too copy the data from
 */
class ObservableRoom(
    fromRoom: Room,
    var onGameStartedChange: ((Boolean) -> Unit)? = null
) {
    val id = fromRoom.id
    val hostUserConnectionId = fromRoom.hostUserConnectionId
    val configuredUserNameList = fromRoom.configuredUserNameList
    val configuredUserNameListMode = fromRoom.configuredUserNameListMode
    val minRoomSize = fromRoom.minRoomSize
    val maxRoomSize = fromRoom.maxRoomSize

    val connectedUsers =
        ObservableMutableList(fromRoom.connectedUsers)
    val gameState = ObservableGameData(fromRoom.gameState)

    var gameStarted: Boolean by Delegates.observable(fromRoom.gameStarted) { _, _, newValue ->
        onGameStartedChange?.invoke(
            newValue
        )
    }

    val dataToBeSentToTheHost =
        ObservableMutableList(fromRoom.dataToBeSentToTheHost)

    /**
     * Constructs a new [Room] object which contains all data of `this` object.
     */
    fun toRoom(): Room {
        val result =
            Room(id, hostUserConnectionId, configuredUserNameList, configuredUserNameListMode, minRoomSize, maxRoomSize)
        result.connectedUsers.clear()
        result.connectedUsers.addAll(connectedUsers)
        result.gameState = gameState.backingGameData
        result.gameStarted = gameStarted
        result.dataToBeSentToTheHost.clear()
        result.dataToBeSentToTheHost.addAll(dataToBeSentToTheHost)
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObservableRoom

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
