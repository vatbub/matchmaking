/*-
 * #%L
 * matchmaking.server
 * %%
 * Copyright (C) 2016 - 2019 Frederik Kammel
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.github.vatbub.matchmaking.server.handlers

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.requests.UpdateGameStateRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.NotAllowedException
import com.github.vatbub.matchmaking.server.roomproviders.RoomProvider
import java.net.Inet4Address
import java.net.Inet6Address

class UpdateGameStateRequestHandler(private val roomProvider: RoomProvider) : RequestHandler {
    override fun needsAuthentication(request: Request): Boolean {
        return true
    }

    override fun canHandle(request: Request): Boolean {
        return request is UpdateGameStateRequest
    }

    override fun handle(request: Request, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
        request as UpdateGameStateRequest

        val roomTransaction = roomProvider.beginTransactionWithRoom(request.roomId) ?: return GetRoomDataResponse(
            request.connectionId,
            null
        )

        if (roomTransaction.room.hostUserConnectionId != request.connectionId) {
            roomTransaction.abort()
            return NotAllowedException(
                "Unable to set the game state: The sending client is not the host, only a game host can start the game",
                request.connectionId
            )
        }

        roomTransaction.room.gameState.replaceContents(request.gameData)
        roomTransaction.room.dataToBeSentToTheHost.removeAll(request.processedData)
        roomTransaction.commit()
        return GetRoomDataResponse(request.connectionId, roomProvider[request.roomId])
    }
}
