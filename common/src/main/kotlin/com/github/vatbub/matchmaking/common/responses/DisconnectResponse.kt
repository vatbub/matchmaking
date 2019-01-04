package com.github.vatbub.matchmaking.common.responses

import com.github.vatbub.matchmaking.common.ResponseImpl
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.requests.DisconnectRequest

/**
 * Response to [DisconnectRequest]
 * @param connectionId The connection id of the requesting client
 * @param disconnectedRooms A list of rooms the user was connected to. The rooms still exist as the user was not the host.
 * @param destroyedRooms A list of rooms the user was connected to. The rooms were destroyed as the user was the host in them.
 */
class DisconnectResponse(connectionId: String?, val disconnectedRooms: List<Room>, val destroyedRooms: List<Room>) :
    ResponseImpl(connectionId, DisconnectResponse::class.qualifiedName!!)