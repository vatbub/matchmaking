package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.github.vatbub.matchmaking.common.*
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.requests.SubscribeToRoomRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.SubscribeToRoomResponse
import com.github.vatbub.matchmaking.jvmclient.EndpointConfiguration
import org.awaitility.Awaitility
import java.io.IOException
import java.util.concurrent.TimeUnit

internal class ResponseHandlerWrapper<T : Response>(val request: Request, val handler: (T) -> Unit)

internal class KryoEndpoint(configuration: EndpointConfiguration.KryoEndpointConfiguration) : ClientEndpoint<EndpointConfiguration.KryoEndpointConfiguration>(configuration) {
    override val isConnected: Boolean
        get() = client.isConnected
    private val client by lazy {
        initializeMinLogRedirect()
        Client()
    }
    private val pendingResponses = mutableListOf<ResponseHandlerWrapper<*>>()
    private var newRoomDataHandlers = mutableMapOf<String, (Room) -> Unit>()
    var disposed = false
        private set

    private object Lock

    private inner class KryoListener : Listener {
        override fun connected(connection: Connection?) {
            logger.info { "Client: Connected to server" }
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

                // Server sent new room state without a prior request. This is only possible for socket connections.
                if (obj is GetRoomDataResponse && obj.responseTo == null) {
                    val roomCopy = obj.room
                    if (roomCopy == null)
                        logger.warn("Received a GetRoomDataResponse which did not include any room data!")
                    else if (newRoomDataHandlers.containsKey(roomCopy.id))
                        newRoomDataHandlers[roomCopy.id]!!.invoke(roomCopy)
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

    override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (Room) -> Unit) {
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
            Awaitility.await().atMost(configuration.timeout.toLong(), TimeUnit.MILLISECONDS).until { this.isConnected }
        }
    }

    override fun terminateConnection() {
        synchronized(Lock) {
            disposed = true
            // Potential fix for ClosedSelectorException in KryoNet
            Thread.sleep(500)
            client.stop()
            client.dispose()
            Awaitility.await().atMost(5L, TimeUnit.SECONDS).until { !this.isConnected }
        }
    }
}