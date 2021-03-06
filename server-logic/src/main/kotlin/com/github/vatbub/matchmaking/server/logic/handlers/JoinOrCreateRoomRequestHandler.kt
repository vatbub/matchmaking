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
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.requests.JoinOrCreateRoomRequest
import com.github.vatbub.matchmaking.common.requests.Operation
import com.github.vatbub.matchmaking.common.responses.JoinOrCreateRoomResponse
import com.github.vatbub.matchmaking.common.responses.Result
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider
import com.github.vatbub.matchmaking.server.logic.roomproviders.canUserJoinOrAbort
import com.github.vatbub.matchmaking.server.logic.roomproviders.data.RoomTransaction
import java.net.Inet4Address
import java.net.Inet6Address

@Suppress("EqualsOrHashCode")
class JoinOrCreateRoomRequestHandler(roomProvider: RoomProvider) : RequestHandlerWithRoomProviderAccess<JoinOrCreateRoomRequest>(roomProvider) {
    override fun equals(other: Any?) = super.equals(other) && (other is JoinOrCreateRoomRequestHandler)
    override fun needsAuthentication(request: JoinOrCreateRoomRequest) = true

    override fun canHandle(request: Request) = request is JoinOrCreateRoomRequest

    override fun handle(request: JoinOrCreateRoomRequest, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
        val connectionId = request.connectionId
                ?: throw IllegalArgumentException("Connection id must not be null when sending a JoinOrCreateRoomRequest")

        val user = User(connectionId, request.userName, sourceIp, sourceIpv6)

        if (request.operation == Operation.JoinRoom || request.operation == Operation.JoinOrCreateRoom) {
            val requestedRoomId = request.roomId
            if (requestedRoomId != null && request.operation != Operation.JoinRoom)
                throw IllegalArgumentException("If roomId is specified, operation must be Operation.JoinRoom")

            val applicableRoomTransaction: RoomTransaction? = if (requestedRoomId != null) {
                val transaction = roomProvider.beginTransactionWithRoom(requestedRoomId)
                        ?: throw IllegalArgumentException("No room with id $requestedRoomId found!")
                if (!transaction.canUserJoinOrAbort(request.userName, request.whitelist, request.blacklist, request.minRoomSize, request.maxRoomSize))
                    throw IllegalArgumentException("Cannot join room with id $requestedRoomId. The user was either not on the whitelist of that room or was on the blacklist, or the room was full, or the game has already been started in that room.")
                transaction
            } else {
                roomProvider.hasApplicableRoom(
                        request.userName,
                        request.whitelist,
                        request.blacklist,
                        request.minRoomSize,
                        request.maxRoomSize
                )
            }
            if (applicableRoomTransaction != null) {
                applicableRoomTransaction.room.connectedUsers.add(user)
                val applicableRoomId = applicableRoomTransaction.room.id
                applicableRoomTransaction.commit()
                return JoinOrCreateRoomResponse(
                        connectionId,
                        Result.RoomJoined,
                        applicableRoomId
                )
            }
        }

        if (request.operation == Operation.CreateRoom || request.operation == Operation.JoinOrCreateRoom) {
            val room = roomProvider.createNewRoom(
                    connectionId,
                    request.whitelist,
                    request.blacklist,
                    request.minRoomSize,
                    request.maxRoomSize
            )
            val transaction = roomProvider.beginTransactionWithRoom(room.id)
                    ?: throw IllegalStateException("Unable to create a room transaction for the newly created room. Please try again.")
            transaction.room.connectedUsers.add(user)
            transaction.commit()
            return JoinOrCreateRoomResponse(connectionId, Result.RoomCreated, room.id)
        }

        // nothing happened
        return JoinOrCreateRoomResponse(connectionId, Result.Nothing, null)
    }
}
