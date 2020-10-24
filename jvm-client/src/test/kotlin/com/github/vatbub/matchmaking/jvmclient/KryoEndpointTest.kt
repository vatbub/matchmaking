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

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.github.vatbub.matchmaking.common.*
import com.github.vatbub.matchmaking.jvmclient.endpoints.KryoEndpoint
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private class DummyKryoServer : DummyServer<EndpointConfiguration.KryoEndpointConfiguration> {
    val kryoServer by lazy {
        initializeMinLogRedirect()
        Server()
    }

    private var disposed = false
    override val isRunning: Boolean
        get() = internalIsRunning
    private var internalIsRunning = false

    private object Lock

    override lateinit var dummyMessageGenerator: (Request) -> Response
    override val endpointConfiguration: EndpointConfiguration.KryoEndpointConfiguration
        get() = EndpointConfiguration.KryoEndpointConfiguration("localhost", KryoCommon.defaultTcpPort, KryoCommon.defaultTcpPort + 50, 99999999)

    override fun start() {
        synchronized(Lock) {
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
        internalIsRunning = true
        logger.info { "Server bound" }


        kryoServer.addListener(object : Listener {
            override fun connected(connection: Connection?) {
                logger.info { "Server: A client connected to me (${this@DummyKryoServer})" }
            }

            override fun disconnected(connection: Connection?) {
                logger.info { "Server: Someone disconnected from me" }
            }

            override fun received(connection: Connection?, receivedObject: Any?) {
                synchronized(Lock) {
                    if (disposed) throw IllegalStateException("Server already disposed, instance thus cannot be used again")
                }
                logger.info { "Dummy server: Received: $receivedObject" }
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
        synchronized(Lock) {
            disposed = true
            Thread.sleep(2000)
            kryoServer.stop()
            internalIsRunning = false
        }
    }
}

class KryoEndpointTest : ClientEndpointTest<KryoEndpoint, EndpointConfiguration.KryoEndpointConfiguration>() {
    override fun newDummyServer(): DummyServer<EndpointConfiguration.KryoEndpointConfiguration> = DummyKryoServer()

    override fun newObjectUnderTest(endpointConfiguration: EndpointConfiguration.KryoEndpointConfiguration, onException: (Throwable) -> Unit) =
            KryoEndpoint(endpointConfiguration, onException)

    override fun newObjectUnderTest(onException: (Throwable) -> Unit) =
            newObjectUnderTest(EndpointConfiguration.KryoEndpointConfiguration("localhost"), onException)

    override fun getCloneOf(instance: KryoEndpoint): KryoEndpoint =
            newObjectUnderTest(EndpointConfiguration.KryoEndpointConfiguration(instance.configuration.host, instance.configuration.tcpPort, instance.configuration.udpPort), instance.onExceptionHappened)

    @Test
    override fun notEqualsTest() {
        val firstInstance = newObjectUnderTest()
        Assertions.assertNotEquals(firstInstance, newObjectUnderTest(EndpointConfiguration.KryoEndpointConfiguration("localhost", KryoCommon.defaultTcpPort + 1, KryoCommon.defaultTcpPort + 2, 5003), firstInstance.onExceptionHappened))
    }
}
