package com.github.vatbub.matchmaking.common.responses

import com.github.vatbub.matchmaking.common.ResponseImpl
import com.github.vatbub.matchmaking.common.requests.JoinOrCreateRoomRequest

/**
 * Response to [JoinOrCreateRoomRequest].
 * @param result Information about what operation was performed on the server
 * @param roomId The id of the room that was created or that the user was assigned to.
 */
class JoinOrCreateRoomResponse(connectionId: String?, val result: Result, val roomId: String) :
    ResponseImpl(connectionId, JoinOrCreateRoomResponse::class.qualifiedName!!)

enum class Result {
    RoomCreated, RoomJoined, Nothing
}