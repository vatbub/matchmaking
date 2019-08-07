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
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

private class DummyKryoServer : DummyServer<EndpointConfiguration.KryoEndpointConfiguration> {
    val kryoServer = Server()
    override lateinit var dummyMessageGenerator: (Request) -> Response
    override val endpointConfiguration: EndpointConfiguration.KryoEndpointConfiguration
        get() = EndpointConfiguration.KryoEndpointConfiguration("localhost")

    override fun start() {
        kryoServer.kryo.registerClasses()
        val configuration = endpointConfiguration
        val udpPort = configuration.udpPort
        kryoServer.start()
        if (udpPort == null)
            kryoServer.bind(configuration.tcpPort)
        else
            kryoServer.bind(configuration.tcpPort, udpPort)


        kryoServer.addListener(object : Listener() {
            override fun received(connection: Connection?, receivedObject: Any?) {
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
        kryoServer.stop()
    }
}

class KryoEndpointTest : ClientEndpointTest<ClientEndpoint.KryoEndpoint, EndpointConfiguration.KryoEndpointConfiguration>(DummyKryoServer()) {
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
}
