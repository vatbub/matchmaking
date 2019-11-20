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
package com.github.vatbub.matchmaking.common.testing.kryo

import com.github.vatbub.matchmaking.common.KryoCommon
import org.apache.mina.core.service.IoHandler
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.transport.socket.nio.NioSocketConnector
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress

class KryoTestClient(ioHandler: IoHandler, host: InetAddress, tcpPort: Int = KryoCommon.defaultTcpPort, udpPort: Int? = null, timeout: Int = 5000) {
    constructor(listener: IoHandler, kryoTestServer: KryoTestServer) : this(listener, kryoTestServer.ipAddress, kryoTestServer.tcpPort, kryoTestServer.udpPort)

    private val tcpConnector = NioSocketConnector()
    val session: IoSession

    init {
        if (udpPort != null) throw Exception("Not yet supported")

        tcpConnector.filterChain.addLast("codec", ProtocolCodecFilter(ObjectSerializationCodecFactory()))
        tcpConnector.filterChain.addLast("logger", LoggingFilter())
        tcpConnector.handler = ioHandler

        try {
            val connectFuture = tcpConnector.connect(InetSocketAddress(host, tcpPort))!!
            connectFuture.awaitUninterruptibly()
            session = connectFuture.session
        } catch (e: RuntimeException) {
            throw IOException("Failed to connect.", e)
        }
    }

    fun stop() {
        session.closeNow().awaitUninterruptibly()
        tcpConnector.dispose()
    }
}
