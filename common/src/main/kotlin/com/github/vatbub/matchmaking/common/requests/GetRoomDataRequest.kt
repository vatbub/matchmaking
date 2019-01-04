package com.github.vatbub.matchmaking.common.requests

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse

/**
 * This request requests the current data of the specified room
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @param roomId The id of the room to get the data of
 * @see GetRoomDataResponse
 */
class GetRoomDataRequest(connectionId: String?, val roomId: String) :
    Request(connectionId, GetRoomDataRequest::class.qualifiedName!!)