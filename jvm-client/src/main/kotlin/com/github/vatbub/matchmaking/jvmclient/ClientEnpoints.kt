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
import com.github.vatbub.matchmaking.common.registerClasses
import com.github.vatbub.matchmaking.common.requests.SubscribeToRoomRequest
import com.github.vatbub.matchmaking.common.responses.*
import com.github.vatbub.matchmaking.common.data.Room as DataRoom

sealed class ClientEndpoint<T : EndpointConfiguration>(protected val configuration: T) {
    fun <T : Response> sendRequest(request: Request, responseHandler: ((T) -> Unit)) {
        request.requestId = RequestIdGenerator.getNewId()
        sendRequestImpl(request, responseHandler)
    }

    abstract fun <T : Response> sendRequestImpl(request: Request, responseHandler: ((T) -> Unit))
    abstract fun abortRequestsOfType(sampleRequest: Request)
    abstract fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (DataRoom) -> Unit)
    abstract fun terminateConnection()
    abstract val isConnected: Boolean

    internal fun verifyResponseIsNotAnException(response: Response) {
        when (response) {
            is com.github.vatbub.matchmaking.common.responses.AuthorizationException -> throw AuthorizationExceptionWrapper(response)
            is BadRequestException -> throw BadRequestExceptionWrapper(response)
            is InternalServerErrorException -> throw InternalServerErrorExceptionWrapper(response)
            is NotAllowedException -> throw NotAllowedExceptionWrapper(response)
            is UnknownConnectionIdException -> throw UnknownConnectionIdExceptionWrapper(response)
        }
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

        private object Lock

        init {
            client.kryo.registerClasses()
            client.start()
            if (configuration.udpPort == null)
                client.connect(configuration.timeout, configuration.host, configuration.tcpPort)
            else
                client.connect(configuration.timeout, configuration.host, configuration.tcpPort, configuration.udpPort)

            client.addListener(KryoListener())
        }

        private inner class KryoListener : Listener() {
            override fun disconnected(connection: Connection?) {
                super.disconnected(connection) // TODO: Handle reconnects
            }

            override fun received(connection: Connection, obj: Any) {
                synchronized(Lock) {
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
                pendingResponses.add(ResponseHandlerWrapper(request, responseHandler))
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

        override fun terminateConnection() {
            client.stop()
            client.dispose()
        }

    }
}

internal class ResponseHandlerWrapper<T : Response>(val request: Request, val handler: (T) -> Unit)
