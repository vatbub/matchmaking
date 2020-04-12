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
import com.github.vatbub.matchmaking.common.requests.DestroyRoomRequest

/**
 * Response to [DestroyRoomRequest]
 * @param connectionId The connection id of the requesting client
 * @param roomDestroyed `true` if the specified room was found and destroyed, `false` otherwise
 */
class DestroyRoomResponse(connectionId: String?, val roomDestroyed: Boolean, responseTo: String? = null) :
        ResponseImpl(connectionId, DestroyRoomResponse::class.qualifiedName!!, responseTo) {
    override fun copy() = DestroyRoomResponse(connectionId, roomDestroyed, responseTo)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DestroyRoomResponse

        if (roomDestroyed != other.roomDestroyed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + roomDestroyed.hashCode()
        return result
    }

    /**
     * Do not remove! Used by KryoNet.
     */
    @Suppress("unused")
    private constructor() : this(null, false)
}
