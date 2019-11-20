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

import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclassWithExceptionHandlerForMultithreading
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.apache.mina.core.future.DefaultWriteFuture
import org.apache.mina.core.future.WriteFuture
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.net.SocketAddress
import java.util.concurrent.TimeUnit

class KryoSessionWrapperTest : KotlinTestSuperclassWithExceptionHandlerForMultithreading<KryoSessionWrapper>() {
    override fun getCloneOf(instance: KryoSessionWrapper) = KryoSessionWrapper(instance.ioSession)

    override fun newObjectUnderTest() = KryoSessionWrapper(DummyIOSession())

    @Test
    fun sendObjectTcpSyncTest() {
        val tcpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(tcpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectSync(tcpObject)
        Assertions.assertEquals(1, connection.writeCallCount)
        // TODO: Assertions.assertEquals(0, connection.sendUdpCallCount)
    }

    @Test
    @Disabled
    fun sendObjectUdpSyncTest() {
        Assertions.fail<String>("Not implemented yet")
        /*val udpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(allowUdp = true, expectedUdpObject = udpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectSync(udpObject)
        Assertions.assertEquals(0, connection.sendTcpCallCount)
        Assertions.assertEquals(1, connection.sendUdpCallCount)*/
    }

    @Test
    fun sendObjectTcpAsyncTest() {
        val tcpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(tcpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectAsync(tcpObject)
        await().atMost(5, TimeUnit.SECONDS).until { connection.writeCallCount == 1 }
        // TODO: Assertions.assertEquals(0, connection.sendUdpCallCount)
    }

    @Test
    @Disabled
    fun sendObjectUdpAsyncTest() {
        Assertions.fail<String>("Not implemented yet")
        /*val udpObject = GetConnectionIdResponse(TestUtils.defaultConnectionId, TestUtils.defaultPassword)
        val connection = MockConnection(allowUdp = true, expectedUdpObject = udpObject)
        val sessionWrapper = KryoSessionWrapper(connection)
        sessionWrapper.sendObjectAsync(udpObject)
        await().atMost(5, TimeUnit.SECONDS).until { connection.sendUdpCallCount == 1 }
        Assertions.assertEquals(0, connection.sendTcpCallCount)*/
    }

    @Test
    override fun notEqualsTest() {
        val wrapper1 = KryoSessionWrapper(DummyIOSession())
        val wrapper2 = KryoSessionWrapper(DummyIOSession())
        Assertions.assertNotEquals(wrapper1, wrapper2)
    }
}

class MockConnection(private val expectedObject: Any? = null) : DummyIOSession() {
    var writeCallCount = 0
        private set

    override fun write(message: Any?) = write(message, null)

    override fun write(message: Any?, destination: SocketAddress?): WriteFuture {
        writeCallCount++
        Assertions.assertEquals(expectedObject, message)
        val future = DefaultWriteFuture(this)
        Thread {
            Thread.sleep(100)
            future.setValue(0)
        }.start()
        return future
    }
}
