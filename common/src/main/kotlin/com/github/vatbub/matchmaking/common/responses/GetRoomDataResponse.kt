package com.github.vatbub.matchmaking.common.responses

import com.github.vatbub.matchmaking.common.ResponseImpl
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.requests.GetRoomDataRequest
import com.github.vatbub.matchmaking.common.requests.SendDataToHostRequest
import com.github.vatbub.matchmaking.common.requests.StartGameRequest
import com.github.vatbub.matchmaking.common.requests.UpdateGameStateRequest

/**
 * Response to [GetRoomDataRequest], [StartGameRequest], [SendDataToHostRequest] and [UpdateGameStateRequest]
 * Contains all data about the room that was specified in the request.
 * @param connectionId The connection id of the requesting client
 * @param room The room that was specified in the request. Please note that the data in the room does not update automatically. You need to poll the api to get updated data.
 */
class GetRoomDataResponse(connectionId: String?, val room: Room) :
    ResponseImpl(connectionId, GetRoomDataResponse::class.qualifiedName!!)