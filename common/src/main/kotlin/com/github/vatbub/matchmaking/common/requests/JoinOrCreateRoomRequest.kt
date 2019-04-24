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
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.JoinOrCreateRoomResponse

/**
 * Joins or creates a [Room] with the specified criteria
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @param password The requesting client's password as assigned by [GetConnectionIdResponse]
 * @param operation The [Operation] to perform
 * @param userName The user name that was chosen by the player who submitted this request
 * @param whitelist If null ignored, otherwise only the users mentioned in this list will be allowed to join this room
 * @param blacklist If null ignored, otherwise the users mentioned in this list will not be allowed to join this room
 * @param minRoomSize The minimum amount of players required for a game. Important: It is up to the game host to verify whether the current amount of connected users lies within the boundaries. If so, the host must start the game by sending a [StartGameRequest]
 * @param maxRoomSize The maximum amount of players allowed in the room. The server will not assign more than this number of people to this room.
 * @see JoinOrCreateRoomResponse
 */
class JoinOrCreateRoomRequest(
    connectionId: String,
    password: String,
    val operation: Operation,
    val userName: String,
    val whitelist: List<String>? = null,
    val blacklist: List<String>? = null,
    val minRoomSize: Int = 1,
    val maxRoomSize: Int = 2
) : Request(connectionId, password, JoinOrCreateRoomRequest::class.qualifiedName!!) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as JoinOrCreateRoomRequest

        if (operation != other.operation) return false
        if (userName != other.userName) return false
        if (whitelist != other.whitelist) return false
        if (blacklist != other.blacklist) return false
        if (minRoomSize != other.minRoomSize) return false
        if (maxRoomSize != other.maxRoomSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + operation.hashCode()
        result = 31 * result + userName.hashCode()
        result = 31 * result + (whitelist?.hashCode() ?: 0)
        result = 31 * result + (blacklist?.hashCode() ?: 0)
        result = 31 * result + minRoomSize
        result = 31 * result + maxRoomSize
        return result
    }
}

enum class Operation {
    JoinRoom, CreateRoom, JoinOrCreateRoom
}
