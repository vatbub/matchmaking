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
package com.github.vatbub.matchmaking.server.logic.handlers

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.requests.DestroyRoomRequest
import com.github.vatbub.matchmaking.common.responses.DestroyRoomResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider
import java.net.Inet4Address
import java.net.Inet6Address

class DestroyRoomRequestHandler(private val roomProvider: RoomProvider) : RequestHandler {
    override fun needsAuthentication(request: Request): Boolean {
        return true
    }

    override fun canHandle(request: Request): Boolean {
        return request is DestroyRoomRequest
    }

    override fun handle(request: Request, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
        request as DestroyRoomRequest
        val roomToDelete = roomProvider[request.roomId] ?: return DestroyRoomResponse(request.connectionId, false)
        if (roomToDelete.hostUserConnectionId != request.connectionId)
            return NotAllowedException("The sender's connection id does not equal the room's host connection id. Only the host can destroy a room.")

        roomProvider.deleteRoom(request.roomId)
        return DestroyRoomResponse(request.connectionId, true)
    }
}
