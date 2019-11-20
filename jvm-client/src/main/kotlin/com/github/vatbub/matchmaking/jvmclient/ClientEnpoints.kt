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

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.common.requests.SubscribeToRoomRequest
import com.github.vatbub.matchmaking.common.responses.*
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IdleStatus
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.transport.socket.nio.NioSocketConnector
import java.io.IOException
import java.net.InetSocketAddress
import com.github.vatbub.matchmaking.common.data.Room as DataRoom

sealed class ClientEndpoint<T : EndpointConfiguration>(internal val configuration: T) {
    fun <T : Response> sendRequest(request: Request, responseHandler: ((T) -> Unit)) {
        request.requestId = RequestIdGenerator.getNewId()
        sendRequestImpl(request, responseHandler)
    }

    abstract fun <T : Response> sendRequestImpl(request: Request, responseHandler: ((T) -> Unit))
    abstract fun abortRequestsOfType(sampleRequest: Request)
    abstract fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (DataRoom) -> Unit)
    abstract fun connect()
    abstract fun terminateConnection()
    abstract val isConnected: Boolean

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
            get() = session?.isConnected ?: false
        private val tcpConnector = NioSocketConnector()
        private var session: IoSession? = null
        private val pendingResponses = mutableListOf<ResponseHandlerWrapper<*>>()
        private var newRoomDataHandlers = mutableMapOf<String, (DataRoom) -> Unit>()

        private object Lock

        init {
            tcpConnector.filterChain.addLast("codec", ProtocolCodecFilter(ObjectSerializationCodecFactory()))
            tcpConnector.filterChain.addLast("logger", LoggingFilter())
            tcpConnector.handler = KryoListener()
        }

        private inner class KryoListener : IoHandlerAdapter() {
            override fun messageReceived(session: IoSession?, message: Any?) {
                synchronized(Lock) {
                    logger.info("Client: Received: $message")
                    requireNotNull(message)
                    require(message is Response) { "Received an object of illegal type: ${message::class.java}" }
                    this@KryoEndpoint.verifyResponseIsNotAnException(message)

                    if (message is GetRoomDataResponse && message.responseTo == null && message.room != null && newRoomDataHandlers.containsKey(message.room!!.id)) {
                        newRoomDataHandlers[message.room!!.id]!!.invoke(message.room!!)
                        return
                    }

                    if (pendingResponses.size == 0) return
                    var identifiedWrapperIndex = 0
                    for (index in pendingResponses.indices) {
                        if (message.responseTo == pendingResponses[index].request.requestId) {
                            identifiedWrapperIndex = index
                            break
                        }
                    }

                    @Suppress("UNCHECKED_CAST")
                    val wrapper = pendingResponses.removeAt(identifiedWrapperIndex) as ResponseHandlerWrapper<Response>
                    wrapper.handler(message)
                }
            }

            override fun sessionOpened(session: IoSession?) {
                logger.debug("MINA session opened")
            }

            override fun sessionClosed(session: IoSession?) {
                logger.info("MINA session disconnected") // TODO: Handle reconnects
            }

            override fun sessionIdle(session: IoSession?, status: IdleStatus?) {
                logger.info("MINA session status is $status")
                if (status == IdleStatus.BOTH_IDLE)
                    session?.closeNow()
            }

            override fun exceptionCaught(session: IoSession?, cause: Throwable?) {
                logger.error("An error occurred while handling network traffic", cause)
            }
        }

        override fun <T : Response> sendRequestImpl(request: Request, responseHandler: (T) -> Unit) {
            synchronized(Lock) {
                pendingResponses.add(ResponseHandlerWrapper(request, responseHandler))
                logger.info("Client: Sending: $request")
                session?.write(request) ?: throw IllegalStateException("Not connected")
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
            if (configuration.udpPort != null)
                throw Exception("UDP is not yet supported")
            try {
                val connectFuture = tcpConnector.connect(InetSocketAddress(configuration.host, configuration.tcpPort))!!
                connectFuture.awaitUninterruptibly()
                session = connectFuture.session
            } catch (e: RuntimeException) {
                throw IOException("Failed to connect.", e)
            }
        }

        override fun terminateConnection() {
            val closeFuture = session?.closeNow() // ?.awaitUninterruptibly()
            Thread {
                closeFuture?.awaitUninterruptibly()
                println("Session closed")
                Thread {
                    tcpConnector.dispose()
                    println("Connector disposed")
                }.start()
            }.start()


        }
    }
}

internal class ResponseHandlerWrapper<T : Response>(val request: Request, val handler: (T) -> Unit)
