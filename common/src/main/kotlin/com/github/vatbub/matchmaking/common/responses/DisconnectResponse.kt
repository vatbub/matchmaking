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
import com.github.vatbub.matchmaking.common.requests.DisconnectRequest

/**
 * Response to [DisconnectRequest]
 * @param connectionId The connection id of the requesting client
 * @param disconnectedRooms A list of rooms the user was connected to. The rooms still exist as the user was not the host.
 * @param destroyedRooms A list of rooms the user was connected to. The rooms were destroyed as the user was the host in them.
 */
class DisconnectResponse(connectionId: String?, val disconnectedRooms: List<Room>, val destroyedRooms: List<Room>, responseTo: String? = null) :
    ResponseImpl(connectionId, DisconnectResponse::class.qualifiedName!!, responseTo) {
    private constructor():this(null, listOf(), listOf())
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DisconnectResponse

        if (disconnectedRooms != other.disconnectedRooms) return false
        if (destroyedRooms != other.destroyedRooms) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + disconnectedRooms.hashCode()
        result = 31 * result + destroyedRooms.hashCode()
        return result
    }
}
