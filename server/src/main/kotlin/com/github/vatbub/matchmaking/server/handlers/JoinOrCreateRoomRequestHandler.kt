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
import com.github.vatbub.matchmaking.common.requests.JoinOrCreateRoomRequest
import com.github.vatbub.matchmaking.common.requests.Operation
import com.github.vatbub.matchmaking.common.responses.JoinOrCreateRoomResponse
import com.github.vatbub.matchmaking.common.responses.Result
import com.github.vatbub.matchmaking.server.roomproviders.RoomProvider
import java.net.Inet4Address
import java.net.Inet6Address

class JoinOrCreateRoomRequestHandler(private val roomProvider: RoomProvider) : RequestHandler {
    override fun needsAuthentication(request: Request): Boolean {
        return true
    }

    override fun canHandle(request: Request): Boolean {
        return request is JoinOrCreateRoomRequest
    }

    override fun handle(request: Request, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
        request as JoinOrCreateRoomRequest
        val user = User(request.connectionId!!, request.userName, sourceIp, sourceIpv6)

        if (request.connectionId == null)
            throw IllegalArgumentException("Connection id must not be null when sending a JoinOrCreateRoomRequest")
        if (request.operation == Operation.JoinRoom || request.operation == Operation.JoinOrCreateRoom) {
            val applicableRoomTransaction = roomProvider.hasApplicableRoom(
                request.userName,
                request.userList,
                request.userListMode,
                request.minRoomSize,
                request.maxRoomSize
            )
            if (applicableRoomTransaction != null) {
                applicableRoomTransaction.room.connectedUsers.add(user)
                applicableRoomTransaction.commit()
                return JoinOrCreateRoomResponse(
                    request.connectionId,
                    Result.RoomJoined,
                    applicableRoomTransaction.room.id
                )
            }
        }

        if (request.operation == Operation.CreateRoom || request.operation == Operation.JoinOrCreateRoom) {
            val room = roomProvider.createNewRoom(
                request.connectionId!!,
                request.userList,
                request.userListMode,
                request.minRoomSize,
                request.maxRoomSize
            )
            return JoinOrCreateRoomResponse(request.connectionId, Result.RoomCreated, room.id)
        }

        // nothing happened
        return JoinOrCreateRoomResponse(request.connectionId, Result.Nothing, null)
    }

    /*
    connectionId: String?,
    val operation: Operation,
    val userName: String,
    val userList: List<String>? = null,
    val userListMode: UserListMode = UserListMode.Ignore,
    val minRoomSize: Int = 1,
    val maxRoomSize: Int = 1
     */
}
