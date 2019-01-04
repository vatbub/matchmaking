package com.github.vatbub.matchmaking.common.requests

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.responses.DestroyRoomResponse
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException

/**
 * This request must be sent by the game host which causes the specified room to be destroyed.
 * Important: Only the game host may send this request. If the sender is not the host in the specified room, a [NotAllowedException] is returned
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @param roomId The id of the room to destroy
 * @see DestroyRoomResponse
 */
class DestroyRoomRequest(connectionId: String?, val roomId: String) :
    Request(connectionId, DestroyRoomRequest::class.qualifiedName!!)