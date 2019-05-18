/*-
 * #%L
 * matchmaking.standalone-server-launcher
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
package com.github.vatbub.matchmaking.standaloneserverlauncher

import com.esotericsoftware.kryonet.Connection
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclassWithExceptionHandlerForMultithreading
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class KryoSessionWrapperTest : KotlinTestSuperclassWithExceptionHandlerForMultithreading<KryoSessionWrapper>() {
    override fun getCloneOf(instance: KryoSessionWrapper) = KryoSessionWrapper(instance.connection)

    override fun newObjectUnderTest() = KryoSessionWrapper(object : Connection() {})

    @Test
    fun sendObjectTcpSyncTest() {
        val tcpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(expectedTcpObject = tcpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectSync(tcpObject)
        Assertions.assertEquals(1, connection.sendTcpCallCount)
        Assertions.assertEquals(0, connection.sendUdpCallCount)
    }

    @Test
    fun sendObjectUdpSyncTest() {
        val udpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(allowUdp = true, expectedUdpObject = udpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectSync(udpObject)
        Assertions.assertEquals(0, connection.sendTcpCallCount)
        Assertions.assertEquals(1, connection.sendUdpCallCount)
    }

    @Test
    fun sendObjectTcpAsyncTest() {
        val tcpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(expectedTcpObject = tcpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectAsync(tcpObject)
        await().atMost(5, TimeUnit.SECONDS).until { connection.sendTcpCallCount == 1 }
        Assertions.assertEquals(0, connection.sendUdpCallCount)
    }

    @Test
    fun sendObjectUdpAsyncTest() {
        val udpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(allowUdp = true, expectedUdpObject = udpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectAsync(udpObject)
        await().atMost(5, TimeUnit.SECONDS).until { connection.sendUdpCallCount == 1 }
        Assertions.assertEquals(0, connection.sendTcpCallCount)
    }

    @Test
    override fun notEqualsTest() {
        val wrapper1 = KryoSessionWrapper(object : Connection() {})
        val wrapper2 = KryoSessionWrapper(object : Connection() {})
        Assertions.assertNotEquals(wrapper1, wrapper2)
    }
}

class MockConnection(private val allowUdp: Boolean = false, private val expectedTcpObject: Any? = null, private val expectedUdpObject: Any? = null) : Connection() {
    var sendTcpCallCount = 0
        private set
    var sendUdpCallCount = 0
        private set

    override fun sendTCP(objectToSend: Any?): Int {
        sendTcpCallCount++
        Assertions.assertEquals(expectedTcpObject, objectToSend)
        return 0
    }

    override fun sendUDP(objectToSend: Any?): Int {
        if (!allowUdp) throw IllegalStateException("Udp not allowed")
        sendUdpCallCount++
        Assertions.assertEquals(expectedUdpObject, objectToSend)
        return 0
    }
}
