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

/**
 * When in game, clients who are not the host can use this request to send data to the game host.
 * The matchmaking server will append the submitted data to the [Room.dataToBeSentToTheHost] queue.
 * This queue is then distributed to all other clients in the specified room, including the host.
 * The host will then process the data and update [Room.gameState] accordingly, but other clients
 * might use the data to extrapolate changes in [Room.gameState]
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @param password The requesting client's password as assigned by [GetConnectionIdResponse]
 * @param roomId The id of the room to send the data to
 * @param dataToHost The data to be sent to the host
 * @see GetRoomDataResponse
 */
class SendDataToHostRequest(
        connectionId: String,
        password: String,
        val roomId: String,
        val dataToHost: List<GameData>,
        requestId: String? = null
) :
        Request(connectionId, password, SendDataToHostRequest::class.qualifiedName!!, requestId) {
    override fun copy() = SendDataToHostRequest(connectionId!!, password!!, roomId, dataToHost, requestId)

    private constructor():this("", "", "", listOf())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as SendDataToHostRequest

        if (roomId != other.roomId) return false
        if (dataToHost != other.dataToHost) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + roomId.hashCode()
        result = 31 * result + dataToHost.hashCode()
        return result
    }
}
