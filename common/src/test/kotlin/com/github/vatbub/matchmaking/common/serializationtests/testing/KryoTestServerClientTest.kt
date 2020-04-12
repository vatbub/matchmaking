/*-
 * #%L
 * matchmaking.common
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
package com.github.vatbub.matchmaking.common.serializationtests.testing

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.github.vatbub.matchmaking.common.KryoCommon
import com.github.vatbub.matchmaking.common.testing.kryo.KryoTestClient
import com.github.vatbub.matchmaking.common.testing.kryo.KryoTestServer
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class KryoTestServerClientTest {
    @Test
    fun connectAndSendTcp() = doTest(false)

    @Disabled
    @Test
    fun connectAndSendUdp() = doTest(true)

    private fun doTest(useUdp: Boolean) {
        val tcpPort = KryoCommon.defaultTcpPort
        val udpPort = if (useUdp) tcpPort + 1 else null

        val objectToSend = "test"

        var serverConnectedCalled = false
        var serverDisconnectedCalled = false
        var serverReceivedObjectCount = 0
        var clientConnectedCalled = false
        var clientDisconnectedCalled = false
        var clientReceivedObjectCount = 0

        val serverListener = object : Listener {
            override fun connected(connection: Connection?) {
                serverConnectedCalled = true
            }

            override fun disconnected(connection: Connection?) {
                serverDisconnectedCalled = true
            }

            override fun received(connection: Connection?, receivedObject: Any?) {
                serverReceivedObjectCount++
                Assertions.assertEquals(objectToSend, receivedObject)
                if (useUdp)
                    connection?.sendUDP(receivedObject)
                else
                    connection?.sendTCP(receivedObject)
            }
        }

        val clientListener = object : Listener {
            override fun connected(connection: Connection?) {
                clientConnectedCalled = true
            }

            override fun disconnected(connection: Connection?) {
                clientDisconnectedCalled = true
            }

            override fun received(connection: Connection?, receivedObject: Any?) {
                clientReceivedObjectCount++
                Assertions.assertEquals(objectToSend, receivedObject)
            }
        }

        val server = KryoTestServer(serverListener, tcpPort, udpPort)
        val client = KryoTestClient(clientListener, server)

        if (useUdp)
            client.client.sendUDP(objectToSend)
        else
            client.client.sendTCP(objectToSend)

        await().atMost(5, TimeUnit.SECONDS).until { clientReceivedObjectCount > 0 }
        client.client.stop()
        server.server.stop()

        Assertions.assertTrue(serverConnectedCalled)
        Assertions.assertTrue(serverDisconnectedCalled)
        Assertions.assertTrue(serverReceivedObjectCount > 0)

        Assertions.assertTrue(clientConnectedCalled)
        Assertions.assertTrue(clientDisconnectedCalled)
    }
}
