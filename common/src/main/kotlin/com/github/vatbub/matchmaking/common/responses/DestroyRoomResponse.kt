package com.github.vatbub.matchmaking.common.responses

import com.github.vatbub.matchmaking.common.ResponseImpl
import com.github.vatbub.matchmaking.common.requests.DestroyRoomRequest

/**
 * Response to [DestroyRoomRequest]
 * @param connectionId The connection id of the requesting client
 */
class DestroyRoomResponse(connectionId: String?) :
    ResponseImpl(connectionId, DestroyRoomResponse::class.qualifiedName!!)