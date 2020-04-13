package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.esotericsoftware.kryonet.Client
import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.github.vatbub.matchmaking.common.*
import com.github.vatbub.matchmaking.jvmclient.EndpointConfiguration
import org.awaitility.Awaitility
import java.io.IOException
import java.util.concurrent.TimeUnit

class KryoEndpoint(configuration: EndpointConfiguration.KryoEndpointConfiguration) : SocketClientEndpoint<EndpointConfiguration.KryoEndpointConfiguration>(configuration) {
    override val isConnected: Boolean
        get() = client.isConnected
    private val client by lazy {
        initializeMinLogRedirect()
        Client()
    }

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
                processResponse(obj)
            }
        }
    }

    override fun socketSendRequestImpl(request: Request) {
        logger.info { "Client: Sending: $request" }
        logger.info { "Connection id is $client" }
        client.sendTCP(request)
    }

    override fun connect() {
        synchronized(Lock) {
            super.connect()
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
            super.terminateConnection()
            // Potential fix for ClosedSelectorException in KryoNet
            Thread.sleep(500)
            client.stop()
            client.dispose()
            Awaitility.await().atMost(5L, TimeUnit.SECONDS).until { !this.isConnected }
        }
    }
}