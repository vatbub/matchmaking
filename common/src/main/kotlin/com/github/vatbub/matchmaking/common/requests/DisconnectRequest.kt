package com.github.vatbub.matchmaking.common.requests

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse

/**
 * This request shall be sent when a user wishes to disconnect from any games he is currently connected to.
 * If this user is the host of a room, that room will be destroyed.
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @see DisconnectResponse
 */
class DisconnectRequest(connectionId: String?) :
    Request(connectionId, DisconnectRequest::class.qualifiedName!!)