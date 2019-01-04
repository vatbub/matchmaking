package com.github.vatbub.matchmaking.common.requests

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException

/**
 * This request updates [Room.gameState]
 * Important: Only the game host may send this request. If the sender is not the host in the specified room, a [NotAllowedException] is returned
 * @param connectionId The requesting client's connection id as assigned by [GetConnectionIdResponse]
 * @param roomId The id of the room to save the game state to
 * @param gameData The game state to set. Important: This must be the complete game state (not a delta) as it overwrites the entire game state in the specified room.
 * @see GetRoomDataResponse
 */
class UpdateGameStateRequest(connectionId: String?, val roomId: String, val gameData: GameData) :
    Request(connectionId, UpdateGameStateRequest::class.qualifiedName!!)