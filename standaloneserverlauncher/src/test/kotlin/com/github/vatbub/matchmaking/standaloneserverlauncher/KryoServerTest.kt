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
import com.esotericsoftware.kryonet.FrameworkMessage
import com.esotericsoftware.kryonet.Listener
import com.github.vatbub.matchmaking.common.KryoCommon
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.common.responses.BadRequestException
import com.github.vatbub.matchmaking.common.responses.InternalServerErrorException
import com.github.vatbub.matchmaking.common.responses.ServerInteractionException
import com.github.vatbub.matchmaking.common.testing.dummies.DummyRequest
import com.github.vatbub.matchmaking.common.testing.kryo.KryoTestClient
import com.github.vatbub.matchmaking.server.logic.ServerContext
import com.github.vatbub.matchmaking.server.logic.testing.dummies.DynamicRequestHandler
import com.github.vatbub.matchmaking.testutils.KotlinTestSuperclassWithExceptionHandlerForMultithreading
import com.github.vatbub.matchmaking.testutils.TestUtils
import org.awaitility.Awaitility.await
import org.awaitility.core.ConditionTimeoutException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import kotlin.random.Random

open class KryoServerTest : KotlinTestSuperclassWithExceptionHandlerForMultithreading<KryoServer>() {
    override val skipEqualsOtherInstanceTests = true

    override fun getCloneOf(instance: KryoServer): KryoServer {
        throw IllegalStateException("KryoServer instances should not be cloned")
    }

    override fun newObjectUnderTest(): KryoServer {
        if (server == null)
            server = KryoServer(KryoCommon.defaultTcpPort, udpPort)
        return server!!
    }

    private val serverContext = ServerContext()
    private var server: KryoServer? = null
    private var client: KryoTestClient? = null
    private val udpPort: Int?
        get() = if (useUdp()) KryoCommon.defaultTcpPort + 1 else null
    private val requestResponseSetups = mutableListOf<RequestResponseSetup>()

    open fun useUdp(): Boolean = false
    /**
     * Number of times a request is retried when the timeout is reached before throwing an exception
     */
    open val maxRequestRetryCount = 0
    /**
     * Time in milliseconds at which a request times out. If [maxRequestRetryCount] is `>0`, the request will be resent [maxRequestRetryCount] times.
     * A value of null is equal to infinity
     */
    open val requestTimeOut: Long? = 5000

    @AfterEach
    fun shutServerAndClientDown() {
        requestResponseSetups.forEach { await().atMost(10, TimeUnit.SECONDS).until { it.allRequestsProcessed } }
        requestResponseSetups.clear()
        client?.client?.stop()
        server?.server?.stop()
    }

    private fun setServerAndClientUp(clientListener: Listener, tcpPort: Int = KryoCommon.defaultTcpPort, udpPort: Int? = this.udpPort) {
        shutServerAndClientDown()
        server = KryoServer(tcpPort, udpPort, serverContext)
        client = KryoTestClient(clientListener, InetAddress.getLocalHost(), tcpPort, udpPort)
    }

    private fun setServerAndClientForRequestResponseTrafficUp(onUnexpectedObjectReceived: (Any) -> Unit = { Assertions.fail("Unexpected object received: $it") },
                                                              tcpPort: Int = KryoCommon.defaultTcpPort, udpPort: Int? = this.udpPort): RequestResponseSetup {
        val setup = RequestResponseSetup(tcpPort, udpPort, onUnexpectedObjectReceived)
        requestResponseSetups.add(setup)
        return setup
    }

    private data class OnResponseHandlerWrapper(val onResponse: (Response) -> Unit) {
        var isHandled = false
            private set

        fun invoke(response: Response) {
            isHandled = true
            onResponse(response)
        }
    }

    inner class RequestResponseSetup(tcpPort: Int = KryoCommon.defaultTcpPort, private val udpPort: Int?, private val onUnexpectedObjectReceived: (Any) -> Unit) {
        private var receivedObjectsCount = 0
        private val pendingResponses = mutableMapOf<String, OnResponseHandlerWrapper>()
        val allRequestsProcessed: Boolean
            get() = pendingResponses.isEmpty()

        init {
            setServerAndClientUp(tcpPort = tcpPort, udpPort = udpPort, clientListener = object : Listener {
                override fun received(connection: Connection?, receivedObject: Any?) {
                    if (receivedObject == null) return
                    if (receivedObject is FrameworkMessage.KeepAlive) return
                    receivedObjectsCount++
                    logger.info { "[Test Client] receivedObjectsCount = $receivedObjectsCount" }
                    logger.info { "[Test Client] Received: $receivedObject" }
                    if (receivedObject !is Response) return onUnexpectedObjectReceived(receivedObject)
                    val handler = pendingResponses.remove(receivedObject.responseTo)
                            ?: return onUnexpectedObjectReceived(receivedObject)
                    handler.invoke(receivedObject)
                }
            })
        }

        fun doRequest(request: Request, onResponse: (Response) -> Unit) {
            request.requestId = RequestIdGenerator.getNewId()
            val wrapper = OnResponseHandlerWrapper(onResponse)
            pendingResponses[request.requestId!!] = wrapper
            var retryCount = 0
            while (true) {
                if (udpPort != null)
                    client!!.client.sendUDP(request)
                else
                    client!!.client.sendTCP(request)
                val requestTimeOutCopy = requestTimeOut ?: return
                try {
                    await().atMost(requestTimeOutCopy, TimeUnit.MILLISECONDS).until { wrapper.isHandled }
                    break
                } catch (timeoutException: ConditionTimeoutException) {
                    retryCount++
                    if (retryCount > maxRequestRetryCount)
                        throw timeoutException
                    logger.warn { "Resending request..." }
                }
            }
        }
    }

    @Test
    fun noResponseTest() {
        setServerAndClientForRequestResponseTrafficUp()
                .doRequest(DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)) {
                    assertExceptionResponse(InternalServerErrorException(), IllegalStateException("No response generated by server"), 500, it)
                }
    }

    @Test
    fun illegalArgumentExceptionTest() {
        val expectedInnerException = java.lang.IllegalArgumentException("Test exception")
        val handler = DynamicRequestHandler<DummyRequest>({ true }, { false }, { _, _, _ ->
            throw expectedInnerException
        })

        val setup = setServerAndClientForRequestResponseTrafficUp()
        serverContext.messageDispatcher.registerHandler(handler)

        setup.doRequest(DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)) {
            assertExceptionResponse(BadRequestException(), expectedInnerException, 400, it)
        }
    }

    // not supported
    @Test
    override fun notEqualsTest() {
    }

    @Test
    fun internalServerErrorExceptionTest() {
        val expectedInnerException = ArrayIndexOutOfBoundsException("Test exception")
        val handler = DynamicRequestHandler<DummyRequest>({ true }, { false }, { _, _, _ ->
            throw expectedInnerException
        })

        val setup = setServerAndClientForRequestResponseTrafficUp()
        serverContext.messageDispatcher.registerHandler(handler)

        setup.doRequest(DummyRequest(TestUtils.defaultConnectionId, TestUtils.defaultPassword)) {
            assertExceptionResponse(InternalServerErrorException(), expectedInnerException, 500, it)
        }
    }

    private fun assertExceptionResponse(
            expectedOuterException: ServerInteractionException,
            expectedInnerException: Throwable,
            expectedHttpStatusCode: Int,
            actualResponse: Response
    ) {
        assertExceptionResponse(
                expectedOuterException,
                expectedHttpStatusCode,
                """${expectedInnerException.javaClass.name}, ${expectedInnerException.message}""",
                actualResponse
        )
    }

    private fun assertExceptionResponse(
            expectedOuterException: ServerInteractionException,
            expectedHttpStatusCode: Int,
            expectedExceptionMessage: String,
            actualResponse: Response
    ) {
        Assertions.assertEquals(expectedHttpStatusCode, actualResponse.httpStatusCode)
        Assertions.assertEquals(expectedOuterException.className, actualResponse.className)
        Assertions.assertEquals(expectedExceptionMessage, (actualResponse as ServerInteractionException).message)
    }
}

object RequestIdGenerator {
    private val usedIds = mutableListOf<Int>()

    private object Lock

    fun getNewId(): String {
        synchronized(Lock) {
            var id: Int
            do {
                id = Random.nextInt()
            } while (usedIds.contains(id))
            usedIds.add(id)
            return id.toString(16)
        }
    }
}
