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
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.dummies.DummyResponse
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclassWithExceptionHandlerForMultithreading
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

interface DummyServer<T : EndpointConfiguration> {
    var dummyMessageGenerator: (Request) -> Response
    val endpointConfiguration: T
    val isRunning: Boolean
    fun start()
    fun stop()
}

abstract class ClientEndpointTest<T : ClientEndpoint<TEndpointConfiguration>, TEndpointConfiguration : EndpointConfiguration>() : KotlinTestSuperclassWithExceptionHandlerForMultithreading<T>() {
    abstract fun newObjectUnderTest(endpointConfiguration: TEndpointConfiguration): T
    abstract fun newDummyServer(): DummyServer<TEndpointConfiguration>

    var dummyServer: DummyServer<TEndpointConfiguration>? = null
        private set
    var endpointUnderTest: T? = null
        private set

    @BeforeEach
    private fun prepare() {
        prepareServer()
        prepareEndpoint()
    }

    @AfterEach
    private fun stopEverything() {
        val server = dummyServer
        if (server != null) {
            server.stop()
            await().atMost(5, TimeUnit.SECONDS).until { !server.isRunning }
        }
        val previousEndpoint = endpointUnderTest
        if (previousEndpoint != null && previousEndpoint.isConnected) {
            previousEndpoint.terminateConnection()
            await().atMost(5, TimeUnit.SECONDS).until { !previousEndpoint.isConnected }
        }
        Thread.sleep(2000)
    }

    private fun prepareServer() {
        val previousServer = dummyServer
        val server = newDummyServer()

        var exceptionCount = 0
        while (true) {
            try {
                server.start()
                break
            } catch (e: Exception) {
                if (exceptionCount > 10) {
                    logger.warn("Exception while trying to start the dummy server, exception count exceeded, rethrowing the exception", e)
                    throw e
                }

                logger.warn("Exception while trying to start the dummy server, trying again...", e)
                exceptionCount++
                if (previousServer != null) {
                    previousServer.stop()
                    await().atMost(5, TimeUnit.SECONDS).until { !previousServer.isRunning }
                }
            }
        }

        dummyServer = server
    }

    private fun prepareEndpoint() {
        val server = dummyServer
        requireNotNull(server)
        val previousEndpoint = endpointUnderTest
        if (previousEndpoint != null && previousEndpoint.isConnected) {
            previousEndpoint.terminateConnection()
            await().atMost(5, TimeUnit.SECONDS).until { !previousEndpoint.isConnected }
        }
        endpointUnderTest = newObjectUnderTest(server.endpointConfiguration)
    }

    @Test
    fun connectTest() {
        val endpointUnderTest = this.endpointUnderTest!!
        endpointUnderTest.connect()
        await().atMost(5L, TimeUnit.SECONDS).until { endpointUnderTest.isConnected }
    }

    @Test
    fun sendRequestTest() {
        val dummyServer = this.dummyServer!!
        val endpointUnderTest = this.endpointUnderTest!!

        var lastResponse: DummyResponse? = null
        var responseHandlerCalled = false
        dummyServer.dummyMessageGenerator = {
            lastResponse = DummyResponse(it.connectionId, it.requestId)
            lastResponse!!
        }
        val request = DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword, null)

        endpointUnderTest.connect()
        await().atMost(5, TimeUnit.SECONDS).until { endpointUnderTest.isConnected }
        endpointUnderTest.sendRequest<DummyResponse>(request) {
            responseHandlerCalled = true
            Assertions.assertEquals(lastResponse, it)
            Assertions.assertEquals(request.requestId, it.responseTo)
        }

        await().atMost(5L, TimeUnit.SECONDS).until { lastResponse != null }
        await().atMost(5L, TimeUnit.SECONDS).until { responseHandlerCalled }
        Assertions.assertNotNull(request.requestId)
    }

    @Test
    fun terminateConnectionTest() {
        val endpointUnderTest = this.endpointUnderTest!!
        endpointUnderTest.connect()
        await().atMost(5L, TimeUnit.SECONDS).until { endpointUnderTest.isConnected }
        endpointUnderTest.terminateConnection()
        await().atMost(5L, TimeUnit.SECONDS).until { !endpointUnderTest.isConnected }
    }
}
