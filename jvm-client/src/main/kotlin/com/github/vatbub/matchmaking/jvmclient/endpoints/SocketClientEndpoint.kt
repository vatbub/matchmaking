/*-
 * #%L
 * matchmaking.jvm-client
 * %%
 * Copyright (C) 2016 - 2020 Frederik Kammel
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
package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.common.requests.SubscribeToRoomRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.SubscribeToRoomResponse
import com.github.vatbub.matchmaking.jvmclient.EndpointConfiguration

abstract class SocketClientEndpoint<T : EndpointConfiguration>(configuration: T) : ClientEndpoint<T>(configuration) {
    internal val pendingResponses = mutableListOf<ResponseHandlerWrapper<*>>()
    internal val newRoomDataHandlers = mutableMapOf<String, (Room) -> Unit>()
    var disposed = false
        internal set
    internal object Lock

    internal fun processResponse(response:Response){
        this.verifyResponseIsNotAnException(response)

        // Server sent new room state without a prior request. This is only possible for socket connections.
        if (response is GetRoomDataResponse && response.responseTo == null) {
            val roomCopy = response.room
            if (roomCopy == null)
                logger.warn("Received a GetRoomDataResponse which did not include any room data!")
            else if (newRoomDataHandlers.containsKey(roomCopy.id))
                newRoomDataHandlers[roomCopy.id]!!.invoke(roomCopy)
            return
        }

        if (pendingResponses.isEmpty()) return
        val identifiedWrapperIndex: Int =
                pendingResponses.indexOfFirst { response.responseTo == it.request.requestId }

        @Suppress("UNCHECKED_CAST")
        val wrapper = pendingResponses.removeAt(identifiedWrapperIndex) as ResponseHandlerWrapper<Response>
        wrapper.handler(response)
    }

    override fun <T : Response> sendRequestImpl(request: Request, responseHandler: (T) -> Unit) {
        synchronized(Lock) {
            if (disposed) throw IllegalStateException("Client already terminated, please reinstantiate the client before sending more requests")
            pendingResponses.add(ResponseHandlerWrapper(request, responseHandler))
            socketSendRequestImpl(request)
        }
    }

    abstract fun socketSendRequestImpl(request:Request)

    override fun abortRequestsOfType(sampleRequest: Request) {
        synchronized(Lock) {
            pendingResponses.removeIf { it.request.className == sampleRequest.className }
        }
    }

    override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (Room) -> Unit) {
        newRoomDataHandlers[roomId] = newRoomDataHandler
        sendRequest<SubscribeToRoomResponse>(SubscribeToRoomRequest(connectionId, password, roomId)) {}
    }

    override fun connect() {
        if (disposed) throw IllegalStateException("Client already terminated, please reinstantiate the client before connecting again")
    }

    override fun terminateConnection() {
        disposed = true
    }
}
