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
package com.github.vatbub.matchmaking.common.responses

import com.github.vatbub.matchmaking.common.ResponseImpl
import com.github.vatbub.matchmaking.common.requests.JoinOrCreateRoomRequest

/**
 * Response to [JoinOrCreateRoomRequest].
 * @param result Information about what operation was performed on the server
 * @param roomId The id of the room that was created or that the user was assigned to.
 */
class JoinOrCreateRoomResponse(connectionId: String?, val result: Result, val roomId: String?) :
    ResponseImpl(connectionId, JoinOrCreateRoomResponse::class.qualifiedName!!) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as JoinOrCreateRoomResponse

        if (result != other.result) return false
        if (roomId != other.roomId) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = super.hashCode()
        result1 = 31 * result1 + result.hashCode()
        result1 = 31 * result1 + (roomId?.hashCode() ?: 0)
        return result1
    }
}

enum class Result {
    RoomCreated, RoomJoined, Nothing
}
