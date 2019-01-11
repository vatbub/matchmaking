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
    override fun canHandle(request: Request): Boolean {
        return request is JoinOrCreateRoomRequest
    }

    override fun handle(request: Request, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
        request as JoinOrCreateRoomRequest
        val user = User(request.connectionId!!, request.userName, sourceIp, sourceIpv6)

        if (request.connectionId == null)
            throw IllegalArgumentException("Connection id must not be null when sending a JoinOrCreateRoomRequest")
        if (request.operation == Operation.JoinRoom || request.operation == Operation.JoinOrCreateRoom) {
            val applicableRoom = roomProvider.hasApplicableRoom(
                request.userName,
                request.userList,
                request.userListMode,
                request.minRoomSize,
                request.maxRoomSize
            )
            if (applicableRoom != null) {
                applicableRoom.connectedUsers.add(user)
                return JoinOrCreateRoomResponse(request.connectionId, Result.RoomJoined, applicableRoom.id)
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