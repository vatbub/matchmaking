package com.github.vatbub.matchmaking.common.requests

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException

/**
 * This request must be sent by the game host which causes [Room.gameStarted] to be set to `true`.
 * Important: Only the game host may send this request. If the sender is not the host in the specified room, a [NotAllowedException] is returned
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @param roomId The id of the room to start the game in
 * @see GetRoomDataResponse
 */
class StartGameRequest(connectionId: String?, val roomId: String) :
    Request(connectionId, StartGameRequest::class.qualifiedName!!)