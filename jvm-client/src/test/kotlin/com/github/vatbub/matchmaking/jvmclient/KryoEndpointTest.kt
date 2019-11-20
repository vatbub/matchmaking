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

import com.github.vatbub.matchmaking.common.KryoCommon
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.logger
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IdleStatus
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress

private class DummyKryoServer : DummyServer<EndpointConfiguration.KryoEndpointConfiguration> {
    private val ioAcceptor = NioSocketAcceptor()
    override lateinit var dummyMessageGenerator: (Request) -> Response
    override val endpointConfiguration: EndpointConfiguration.KryoEndpointConfiguration
        get() = EndpointConfiguration.KryoEndpointConfiguration("localhost")

    override fun start() {
        val configuration = endpointConfiguration
        val udpPort = configuration.udpPort
        if (udpPort != null)
            throw Exception("UDP not yet supported")
        // TODO: Specify the logging levels for each event type (see http://mina.apache.org/mina-project/userguide/ch12-logging-filter/ch12-logging-filter.html)

        ioAcceptor.filterChain.addLast("logger", LoggingFilter())
        ioAcceptor.filterChain.addLast("codec", ProtocolCodecFilter(ObjectSerializationCodecFactory()))

        ioAcceptor.handler = object : IoHandlerAdapter() {
            override fun sessionClosed(session: IoSession?) {
                logger.info("Dummy MINA Server: closing session...")
            }

            override fun messageReceived(session: IoSession?, message: Any?) {
                logger.info("Dummy server: Received: $message")
                requireNotNull(session)
                requireNotNull(message)
                require(message is Request) { "Message of illegal type: ${message::class.java.name}" }
                session.write(dummyMessageGenerator(message))
            }
        }

        ioAcceptor.sessionConfig.readBufferSize = 2048
        ioAcceptor.sessionConfig.setIdleTime(IdleStatus.BOTH_IDLE, 10)

        ioAcceptor.bind(InetSocketAddress(configuration.tcpPort))
        logger.info("MINA dummy tcp server bound to port ${configuration.tcpPort}")
    }

    override fun stop() {
        ioAcceptor.dispose(true)
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
