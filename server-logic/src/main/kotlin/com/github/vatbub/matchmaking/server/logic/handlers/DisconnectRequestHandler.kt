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
import com.github.vatbub.matchmaking.common.requests.DisconnectRequest
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider
import java.net.Inet4Address
import java.net.Inet6Address

@Suppress("EqualsOrHashCode")
class DisconnectRequestHandler(roomProvider: RoomProvider) : RequestHandlerWithRoomProviderAccess<DisconnectRequest>(roomProvider) {
    override fun equals(other: Any?) = super.equals(other) && (other is DisconnectRequestHandler)
    override fun needsAuthentication(request: DisconnectRequest) = true

    override fun canHandle(request: Request) = request is DisconnectRequest

    override fun handle(request: DisconnectRequest, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
        val disconnectedRoomIds = mutableListOf<String>()
        // Jacoco thinks that we missed something here in the Unit tests but I can't think of anything
        roomProvider.beginTransactionsForRoomsWithFilter({ it.connectedUsers.find { user -> user.connectionId == request.connectionId } != null },
                { roomTransaction ->
                    disconnectedRoomIds.add(roomTransaction.room.id)
                    val usersToDisconnect = roomTransaction.room.connectedUsers.filter { it.connectionId == request.connectionId }
                    roomTransaction.room.connectedUsers.removeAll(usersToDisconnect)
                    roomTransaction.commit()
                })

        val deletedRooms = roomProvider.deleteRooms { it.hostUserConnectionId == request.connectionId }
        val disconnectedRooms = roomProvider.getRoomsById(disconnectedRoomIds).toList()

        return DisconnectResponse(request.connectionId, disconnectedRooms, deletedRooms)
    }
}
