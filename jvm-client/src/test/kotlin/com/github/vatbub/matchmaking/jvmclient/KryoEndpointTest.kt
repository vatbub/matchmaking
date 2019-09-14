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

import com.esotericsoftware.kryonet.*
import com.esotericsoftware.kryonet.Client
import com.github.vatbub.matchmaking.common.*
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

private class DummyKryoServer : DummyServer<EndpointConfiguration.KryoEndpointConfiguration> {
    val kryoServer = Server()
    private var disposed = false
    private object Lock
    override lateinit var dummyMessageGenerator: (Request) -> Response
    override val endpointConfiguration: EndpointConfiguration.KryoEndpointConfiguration
        get() = EndpointConfiguration.KryoEndpointConfiguration("localhost", KryoCommon.defaultTcpPort, KryoCommon.defaultTcpPort + 50, 99999999)

    override fun start() {
        synchronized(Lock){
            if (disposed) throw IllegalStateException("Server already disposed, instance thus cannot be used again")
        }
        val configuration = endpointConfiguration
        val udpPort = configuration.udpPort
        kryoServer.start()
        kryoServer.kryo.registerClasses()
        if (udpPort == null)
            kryoServer.bind(configuration.tcpPort)
        else
            kryoServer.bind(configuration.tcpPort, udpPort)
        logger.info("Server bound")


        kryoServer.addListener(object : Listener() {
            override fun connected(connection: Connection?) {
                logger.info("Server: A client connected to me (${this@DummyKryoServer})")
            }

            override fun disconnected(connection: Connection?) {
                logger.info("Server: Someone disconnected from me")
            }

            override fun received(connection: Connection?, receivedObject: Any?) {
                synchronized(Lock){
                    if (disposed) throw IllegalStateException("Server already disposed, instance thus cannot be used again")
                }
                logger.info("Dummy server: Received: $receivedObject")
                if (receivedObject is FrameworkMessage.KeepAlive) return
                receivedObject as Request
                val response = dummyMessageGenerator(receivedObject)
                try {
                    connection!!.sendUDP(response)
                } catch (e: IllegalStateException) {
                    connection!!.sendTCP(response)
                }
            }
        })
    }

    override fun stop() {
        synchronized(Lock){
            disposed = true
            kryoServer.stop()
        }
    }
}

class KryoEndpointTest : ClientEndpointTest<ClientEndpoint.KryoEndpoint, EndpointConfiguration.KryoEndpointConfiguration>() {
    override fun newDummyServer(): DummyServer<EndpointConfiguration.KryoEndpointConfiguration> = DummyKryoServer()

    override fun newObjectUnderTest(endpointConfiguration: EndpointConfiguration.KryoEndpointConfiguration) =
            ClientEndpoint.KryoEndpoint(endpointConfiguration)

    override fun newObjectUnderTest() =
            newObjectUnderTest(EndpointConfiguration.KryoEndpointConfiguration("localhost"))

    override fun getCloneOf(instance: ClientEndpoint.KryoEndpoint): ClientEndpoint.KryoEndpoint {
        return newObjectUnderTest(EndpointConfiguration.KryoEndpointConfiguration(instance.configuration.host, instance.configuration.tcpPort, instance.configuration.udpPort))
    }

    @Test
    override fun notEqualsTest() {
        Assertions.assertNotEquals(newObjectUnderTest(), newObjectUnderTest(EndpointConfiguration.KryoEndpointConfiguration("localhost", KryoCommon.defaultTcpPort + 1, KryoCommon.defaultTcpPort + 2, 5003)))
    }

    @Test
    fun dummyConnectTest() {
        val server = Server()
        val client = Client()

        server.start()
        client.start()

        server.kryo.registerClasses()
        client.kryo.registerClasses()

        server.bind(KryoCommon.defaultTcpPort + 10)
        client.connect(5000, "localhost", KryoCommon.defaultTcpPort + 10)

        await().atMost(5, TimeUnit.SECONDS).until { client.isConnected }

        var serverReceived = false
        var clientReceived = false

        server.addListener(object : Listener() {
            override fun received(connection: Connection?, receivedObject: Any?) {
                logger.info("Server: Received: $receivedObject")
                if (receivedObject is FrameworkMessage.KeepAlive) return
                serverReceived = true
                connection!!.sendTCP(DummyResponse())
            }
        })

        client.addListener(object : Listener() {
            override fun received(connection: Connection?, receivedObject: Any?) {
                logger.info("Client: Received: $receivedObject")
                if (receivedObject is FrameworkMessage.KeepAlive) return
                clientReceived = true
            }
        })

        client.sendTCP(DummyRequest())

        await().atMost(5, TimeUnit.SECONDS).until { serverReceived }
        await().atMost(5, TimeUnit.SECONDS).until { clientReceived }
    }

    @Test
    fun dummyConnectTest2() {
        val server = Server()
        val tempEndpoint = ClientEndpoint.KryoEndpoint(EndpointConfiguration.KryoEndpointConfiguration("localhost", KryoCommon.defaultTcpPort + 20))

        server.start()

        server.kryo.registerClasses()

        server.bind(KryoCommon.defaultTcpPort + 20)
        tempEndpoint.connect()

        await().atMost(5, TimeUnit.SECONDS).until { tempEndpoint.isConnected }

        var serverReceived = false
        var clientReceived = false

        server.addListener(object : Listener() {
            override fun received(connection: Connection?, receivedObject: Any?) {
                logger.info("Server: Received: $receivedObject")
                if (receivedObject is FrameworkMessage.KeepAlive) return
                serverReceived = true
                connection!!.sendTCP(DummyResponse())
            }
        })

        tempEndpoint.sendRequest<DummyResponse>(DummyRequest()) {
            logger.info("Client: Received: $it")
            clientReceived = true
        }

        await().atMost(5, TimeUnit.SECONDS).until { serverReceived }
        await().atMost(5, TimeUnit.SECONDS).until { clientReceived }

        tempEndpoint.terminateConnection()
        server.stop()
    }

    @Test
    fun dummyConnectTest3() {
        logger.info("Check 1")
        val dummyServer = prepareServer()
        logger.info("Check 2")
        val tempEndpoint = prepareEndpoint(dummyServer)
        logger.info("Check 3")

        dummyServer.start()
        logger.info("Check 4")
        tempEndpoint.connect()
        logger.info("Check 5")

        await().atMost(5, TimeUnit.SECONDS).until { tempEndpoint.isConnected }

        var serverReceived = false
        var clientReceived = false

        dummyServer.dummyMessageGenerator = {
            serverReceived = true
            DummyResponse()
        }

        tempEndpoint.sendRequest<DummyResponse>(DummyRequest()) {
            clientReceived = true
        }

        await().atMost(5, TimeUnit.SECONDS).until { serverReceived }
        await().atMost(5, TimeUnit.SECONDS).until { clientReceived }

        tempEndpoint.terminateConnection()
        dummyServer.stop()
    }
}
