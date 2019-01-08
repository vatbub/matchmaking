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
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.requests.GetRoomDataRequest
import com.github.vatbub.matchmaking.common.requests.SendDataToHostRequest
import com.github.vatbub.matchmaking.common.requests.StartGameRequest
import com.github.vatbub.matchmaking.common.requests.UpdateGameStateRequest

/**
 * Response to [GetRoomDataRequest], [StartGameRequest], [SendDataToHostRequest] and [UpdateGameStateRequest]
 * Contains all data about the room that was specified in the request.
 * @param connectionId The connection id of the requesting client
 * @param room The room that was specified in the request. Please note that the data in the room does not update automatically. You need to poll the api to get updated data.
 */
class GetRoomDataResponse(connectionId: String?, val room: Room) :
    ResponseImpl(connectionId, GetRoomDataResponse::class.qualifiedName!!) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as GetRoomDataResponse

        if (room != other.room) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + room.hashCode()
        return result
    }
}
