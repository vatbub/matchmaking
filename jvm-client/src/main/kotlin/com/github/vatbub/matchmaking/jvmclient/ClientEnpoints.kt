/*-
 * #%L
 * matchmaking.jvm-client
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
package com.github.vatbub.matchmaking.jvmclient

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.common.registerClasses
import com.github.vatbub.matchmaking.common.requests.SubscribeToRoomRequest
import com.github.vatbub.matchmaking.common.responses.*
import org.awaitility.Awaitility.await
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.github.vatbub.matchmaking.common.data.Room as DataRoom

/**
 * Internal classes which handle the traffic to the server.
 * You should not need to call these classes directly, as they operate on a very low level.
 * Use [Client] instead.
 *
 * @see Client
 */
sealed class ClientEndpoint<T : EndpointConfiguration>(internal val configuration: T) {
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
    abstract fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (DataRoom) -> Unit)

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

    class WebsocketEndpoint(configuration: EndpointConfiguration.WebsocketEndpointConfig) : ClientEndpoint<EndpointConfiguration.WebsocketEndpointConfig>(configuration) {
        override val isConnected: Boolean
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

        override fun <T : Response> sendRequestImpl(request: Request, responseHandler: (T) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun abortRequestsOfType(sampleRequest: Request) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (DataRoom) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun connect() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun terminateConnection() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    class HttpPollingEndpoint(configuration: EndpointConfiguration.HttpPollingEndpointConfig) : ClientEndpoint<EndpointConfiguration.HttpPollingEndpointConfig>(configuration) {
        override val isConnected: Boolean
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

        override fun <T : Response> sendRequestImpl(request: Request, responseHandler: (T) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun abortRequestsOfType(sampleRequest: Request) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (DataRoom) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun connect() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun terminateConnection() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    class KryoEndpoint(configuration: EndpointConfiguration.KryoEndpointConfiguration) : ClientEndpoint<EndpointConfiguration.KryoEndpointConfiguration>(configuration) {
        override val isConnected: Boolean
            get() = client.isConnected
        private val client = Client()
        private val pendingResponses = mutableListOf<ResponseHandlerWrapper<*>>()
        private var newRoomDataHandlers = mutableMapOf<String, (DataRoom) -> Unit>()
        var disposed = false
            private set

        private object Lock

        private inner class KryoListener : Listener() {
            override fun connected(connection: Connection?) {
                logger.info { "Client: Connected to server" }
            }

            override fun idle(connection: Connection?) {
                // logger.info("Client: Connection to server is idle")
            }

            override fun disconnected(connection: Connection?) {
                logger.info { "Client: Disconnected from server" }
                if (disposed) return
                synchronized(Lock) {
                    if (disposed) return
                    Thread {
                        logger.info { "Trying to reconnect..." }
                        try {
                            client.reconnect()
                        } catch (e: IOException) {
                            logger.warn(e) { "Unable to reconnect due to an IOException" }
                        }
                    }
                }
            }

            override fun received(connection: Connection, obj: Any) {
                synchronized(Lock) {
                    logger.info { "Client: Received: $obj" }
                    if (obj is FrameworkMessage.KeepAlive) return
                    if (obj !is Response) throw IllegalArgumentException("Received an object of illegal type: ${obj.javaClass.name}")
                    this@KryoEndpoint.verifyResponseIsNotAnException(obj)

                    if (obj is GetRoomDataResponse && obj.responseTo == null && obj.room != null && newRoomDataHandlers.containsKey(obj.room!!.id)) {
                        newRoomDataHandlers[obj.room!!.id]!!.invoke(obj.room!!)
                        return
                    }

                    if (pendingResponses.size == 0) return
                    var identifiedWrapperIndex = 0
                    for (index in pendingResponses.indices) {
                        if (obj.responseTo == pendingResponses[index].request.requestId) {
                            identifiedWrapperIndex = index
                            break
                        }
                    }

                    @Suppress("UNCHECKED_CAST")
                    val wrapper = pendingResponses.removeAt(identifiedWrapperIndex) as ResponseHandlerWrapper<Response>
                    wrapper.handler(obj)
                }
            }
        }

        override fun <T : Response> sendRequestImpl(request: Request, responseHandler: (T) -> Unit) {
            synchronized(Lock) {
                if (disposed) throw IllegalStateException("Client already terminated, please reinstantiate the client before sending more requests")
                pendingResponses.add(ResponseHandlerWrapper(request, responseHandler))
                logger.info { "Client: Sending: $request" }
                logger.info { "Connection id is $client" }
                client.sendTCP(request)
            }
        }

        override fun abortRequestsOfType(sampleRequest: Request) {
            synchronized(Lock) {
                pendingResponses.removeIf { it.request.className == sampleRequest.className }
            }
        }

        override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (DataRoom) -> Unit) {
            newRoomDataHandlers[roomId] = newRoomDataHandler
            sendRequest<SubscribeToRoomResponse>(SubscribeToRoomRequest(connectionId, password, roomId)) {}
        }

        override fun connect() {
            synchronized(Lock) {
                if (disposed) throw IllegalStateException("Client already terminated, please reinstantiate the client before connecting again")
                client.start()
                client.kryo.registerClasses()
                if (configuration.udpPort == null)
                    client.connect(configuration.timeout, configuration.host, configuration.tcpPort)
                else
                    client.connect(configuration.timeout, configuration.host, configuration.tcpPort, configuration.udpPort)
                client.start()
                client.addListener(KryoListener())
                await().atMost(configuration.timeout.toLong(), TimeUnit.MILLISECONDS).until { this.isConnected }
            }
        }

        override fun terminateConnection() {
            synchronized(Lock) {
                disposed = true
                client.stop()
                client.dispose()
                await().atMost(5L, TimeUnit.SECONDS).until { !this.isConnected }
            }
        }
    }
}

internal class ResponseHandlerWrapper<T : Response>(val request: Request, val handler: (T) -> Unit)
