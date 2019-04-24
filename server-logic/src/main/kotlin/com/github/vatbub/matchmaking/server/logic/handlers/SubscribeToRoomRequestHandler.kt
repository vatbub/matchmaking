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
package com.github.vatbub.matchmaking.server.logic.handlers

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.requests.SubscribeToRoomRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.SubscribeToRoomResponse
import com.github.vatbub.matchmaking.server.logic.roomproviders.OnCommitRoomTransactionListener
import com.github.vatbub.matchmaking.server.logic.roomproviders.RoomProvider
import com.github.vatbub.matchmaking.server.logic.sockets.Session
import java.net.Inet4Address
import java.net.Inet6Address

class SubscribeToRoomRequestHandler(private val roomProvider: RoomProvider) : RequestHandlerWithWebsocketSupport() {
    private val roomListeners = mutableMapOf<Session, RoomListener>()

    override val requiresSocket = true

    override fun canHandle(request: Request): Boolean {
        return request is SubscribeToRoomRequest
    }

    override fun needsAuthentication(request: Request): Boolean {
        return true
    }

    override fun handle(request: Request, sourceIp: Inet4Address?, sourceIpv6: Inet6Address?): Response {
        throw IllegalStateException("Cannot handle non-websocket requests")
    }

    override fun handle(
            session: Session,
            request: Request,
            sourceIp: Inet4Address?,
            sourceIpv6: Inet6Address?
    ): Response {
        request as SubscribeToRoomRequest
        val roomListener = RoomListener(request.connectionId, request.roomId, session)
        roomListeners[session] = roomListener
        roomProvider.onCommitRoomTransactionListeners.add(roomListener)
        return SubscribeToRoomResponse(request.connectionId)
    }

    override fun onSessionClosed(session: Session) {
        val listener = roomListeners[session] ?: return
        roomProvider.onCommitRoomTransactionListeners.remove(listener)
    }

    private class RoomListener(val connectionId: String?, val roomId: String, val session: Session) :
            OnCommitRoomTransactionListener {
        override fun onCommit(room: Room) {
            if (room.id != roomId)
                return

            val roomResponse = GetRoomDataResponse(connectionId, room)
            session.sendObjectAsync(roomResponse)
        }
    }
}
