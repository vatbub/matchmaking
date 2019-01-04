package com.github.vatbub.matchmaking.common.requests

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse

/**
 * When in game, clients who are not the host can use this request to send data to the game host.
 * The matchmaking server will append the submitted data to the [Room.dataToBeSentToTheHost] queue.
 * This queue is then distributed to all other clients in the specified room, including the host.
 * The host will then process the data and update [Room.gameState] accordingly, but other clients
 * might use the data to extrapolate changes in [Room.gameState]
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @param roomId The id of the room to send the data to
 * @param dataToHost The data to be sent to the host
 * @see GetRoomDataResponse
 */
class SendDataToHostRequest(connectionId: String?, val roomId: String, val dataToHost: Sequence<GameData>) :
    Request(connectionId, SendDataToHostRequest::class.qualifiedName!!)