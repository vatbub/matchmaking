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
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclassWithExceptionHandlerForMultithreading
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.*
import java.util.concurrent.TimeUnit

interface DummyServer<T : EndpointConfiguration> {
    var dummyMessageGenerator: (Request) -> Response
    val endpointConfiguration: T
    fun start()
    fun stop()
}

abstract class ClientEndpointTest<T : ClientEndpoint<TEndpointConfiguration>, TEndpointConfiguration : EndpointConfiguration>(private val dummyServer: DummyServer<TEndpointConfiguration>) : KotlinTestSuperclassWithExceptionHandlerForMultithreading<T>() {
    abstract fun newObjectUnderTest(endpointConfiguration: TEndpointConfiguration): T

    private lateinit var endpointUnderTest: T

    @BeforeAll
    fun startServer() = dummyServer.start()

    @AfterAll
    fun stopServer() = dummyServer.stop()

    @BeforeEach
    fun prepareEndpoint() {
        endpointUnderTest = newObjectUnderTest(dummyServer.endpointConfiguration)
    }

    @AfterEach
    fun terminateConnection() {
        endpointUnderTest.terminateConnection()
    }

    @Test
    fun connectTest() {
        endpointUnderTest.connect()
        await().atMost(5L, TimeUnit.SECONDS).until { endpointUnderTest.isConnected }
    }

    @Test
    // TODO
    @Disabled("Cannot bind a server and a client on the same machine to the same port")
    fun sendRequestTest() {
        endpointUnderTest.connect()
        var lastResponse: DummyResponse? = null
        var responseHandlerCalled = false
        dummyServer.dummyMessageGenerator = {
            lastResponse = DummyResponse(it.connectionId, it.requestId)
            lastResponse!!
        }
        val request = DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, null)
        newObjectUnderTest().sendRequest<DummyResponse>(request) {
            responseHandlerCalled = true
            Assertions.assertSame(lastResponse, it)
            Assertions.assertEquals(request.requestId, it.responseTo)
        }

        await().atMost(5L, TimeUnit.SECONDS).until { lastResponse != null }
        await().atMost(5L, TimeUnit.SECONDS).until { responseHandlerCalled }
        Assertions.assertNotNull(request.requestId)
    }

    @Test
    fun terminateConnectionTest() {
        endpointUnderTest.connect()
        endpointUnderTest.terminateConnection()
        await().atMost(5L, TimeUnit.SECONDS).until { !endpointUnderTest.isConnected }
    }
}
