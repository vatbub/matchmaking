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
package com.github.vatbub.matchmaking.server.handlers

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.requests.DisconnectRequest
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.server.roomproviders.RoomProvider
import java.net.Inet4Address
import java.net.Inet6Address

class DisconnectRequestHandler(private val roomProvider: RoomProvider) : RequestHandler {
    override fun needsAuthentication(request: Request): Boolean {
        return true
    }

    override fun canHandle(request: Request): Boolean {
        return request is DisconnectRequest
    }

    override fun handle(request: Request, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
        request as DisconnectRequest
        val roomIdsToDelete = mutableListOf<String>()
        val disconnectedRoomIds = mutableListOf<String>()
        roomProvider.beginTransactionForAllRooms { roomTransaction ->
            if (roomTransaction.room.hostUserConnectionId == request.connectionId)
                roomIdsToDelete.add(roomTransaction.room.id)

            val usersToDisconnect = mutableListOf<User>()
            for (user in roomTransaction.room.connectedUsers) {
                if (user.connectionId == request.connectionId) {
                    if (!disconnectedRoomIds.contains(roomTransaction.room.id))
                        disconnectedRoomIds.add(roomTransaction.room.id)
                    usersToDisconnect.add(user)
                }
            }

            roomTransaction.room.connectedUsers.removeAll(usersToDisconnect)
            roomTransaction.commit()
        }

        val deletedRooms = roomProvider.deleteRooms(*roomIdsToDelete.toTypedArray())
        val disconnectedRooms = roomProvider.getRoomsById(disconnectedRoomIds).toList()

        return DisconnectResponse(request.connectionId, disconnectedRooms, deletedRooms)
    }
}
