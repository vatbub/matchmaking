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
package com.github.vatbub.matchmaking.common.requests

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException

/**
 * This request updates [Room.gameState]
 * Important: Only the game host may send this request. If the sender is not the host in the specified room, a [NotAllowedException] is returned
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @param password The requesting client's password as assigned by [GetConnectionIdResponse]
 * @param roomId The id of the room to save the game state to
 * @param gameData The game state to set. Important: This must be the complete game state (not a delta) as it overwrites the entire game state in the specified room.
 * @param processedData The data coming from [Room.dataToBeSentToTheHost] that was used to generate the new game state. The server will then remove this data from [Room.dataToBeSentToTheHost]
 * @see GetRoomDataResponse
 */
class UpdateGameStateRequest(
        connectionId: String,
        password: String,
        val roomId: String,
        val gameData: GameData,
        val processedData: List<GameData>,
        requestId: String? = null
) :
        Request(connectionId, password, UpdateGameStateRequest::class.qualifiedName!!, requestId) {
    override fun copy() = UpdateGameStateRequest(connectionId!!, password!!, roomId, gameData, processedData, requestId)

    /**
     * Do not remove! Used by KryoNet.
     */
    @Suppress("unused")
    private constructor() : this("", "", "", GameData(""), listOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as UpdateGameStateRequest

        if (roomId != other.roomId) return false
        if (gameData != other.gameData) return false
        if (processedData != other.processedData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + roomId.hashCode()
        result = 31 * result + gameData.hashCode()
        result = 31 * result + processedData.hashCode()
        return result
    }
}
