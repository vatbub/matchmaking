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

import com.esotericsoftware.kryonet.Client
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.responses.*
import com.github.vatbub.matchmaking.jvmclient.*

/**
 * Internal classes which handle the traffic to the server.
 * You should not need to call these classes directly, as they operate on a very low level.
 * Use [Client] instead.
 *
 * @see Client
 */
abstract class ClientEndpoint<T : EndpointConfiguration>(internal val configuration: T) {
    /**
     * Assigns a [Request.requestId] to the request and then forwards the request to the implementation.
     * @param request The request to be sent
     * @param responseHandler Callback which handles the response returned by the server
     */
    fun <T : Response> sendRequest(request: Request, responseHandler: ((T) -> Unit)) {
        request.requestId = RequestIdGenerator.getNewId()
        sendRequestImpl(request, responseHandler)
    }

    /**
     * Sends the specified request to the server and calls the [responseHandler] once the response from the server has arrived.
     *
     * **IMPORTANT:** This method does not need to modify the request in any way, all necessary modifications (like adding a request id) have already been done.
     * @param request The request to be sent. The request does not need to be modified, it can be sent as is.
     * @param responseHandler The handler to be called once the server sent a response to the request.
     */
    abstract fun <T : Response> sendRequestImpl(request: Request, responseHandler: ((T) -> Unit))

    /**
     * If called, all pending requests of the specified type shall be aborted.
     * @param sampleRequest A sample request to show the type of request to be aborted. All requests of the same type shall be aborted. All other request parameters shall be ignored when determining which requests to abort.
     */
    abstract fun abortRequestsOfType(sampleRequest: Request)

    /**
     * Subscribes to changes in a room.
     * @param connectionId The connection id of the client.
     * @param password The password of the client.
     * @param roomId The id to the room to subscribe to.
     * @param newRoomDataHandler Callback which is called whenever any parameter of the specified room changes.
     */
    abstract fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (Room) -> Unit)

    /**
     * Establishes a connection to the server.
     * No additional requests must be sent.
     */
    abstract fun connect()

    /**
     * Terminates the connection to the server gracefully.
     */
    abstract fun terminateConnection()

    /**
     * Checks whether the endpoint is connected.
     */
    abstract val isConnected: Boolean

    /**
     * If the specified [response] is a subclass of [ServerInteractionException], the corresponding exception will be thrown.
     * No-op otherwise.
     */
    internal fun verifyResponseIsNotAnException(response: Response) {
        when (response) {
            is AuthorizationException -> throw AuthorizationExceptionWrapper(response)
            is BadRequestException -> throw BadRequestExceptionWrapper(response)
            is InternalServerErrorException -> throw InternalServerErrorExceptionWrapper(response)
            is NotAllowedException -> throw NotAllowedExceptionWrapper(response)
            is UnknownConnectionIdException -> throw UnknownConnectionIdExceptionWrapper(response)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClientEndpoint<*>) return false

        if (configuration != other.configuration) return false

        return true
    }

    override fun hashCode(): Int {
        return configuration.hashCode()
    }
}
