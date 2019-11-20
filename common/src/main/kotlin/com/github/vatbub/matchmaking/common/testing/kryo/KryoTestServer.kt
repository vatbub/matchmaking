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
import com.github.vatbub.matchmaking.common.logger
import org.apache.mina.core.service.IoHandler
import org.apache.mina.core.session.IdleStatus
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import java.net.InetAddress
import java.net.InetSocketAddress

class KryoTestServer(listener: IoHandler, val tcpPort: Int = KryoCommon.defaultTcpPort, val udpPort: Int? = null) {
    private val ioAcceptor = NioSocketAcceptor()
    val ipAddress = InetAddress.getLocalHost()!!

    init {
        if (udpPort != null)
            throw Exception("UDP not yet supported")
        // TODO: Specify the logging levels for each event type (see http://mina.apache.org/mina-project/userguide/ch12-logging-filter/ch12-logging-filter.html)
        ioAcceptor.filterChain.addLast("logger", LoggingFilter())
        ioAcceptor.filterChain.addLast("codec", ProtocolCodecFilter(ObjectSerializationCodecFactory()))
        ioAcceptor.handler = listener
        ioAcceptor.sessionConfig.readBufferSize = 2048
        ioAcceptor.sessionConfig.setIdleTime(IdleStatus.BOTH_IDLE, 10)

        ioAcceptor.bind(InetSocketAddress(tcpPort))
        logger.info("MINA test tcp server bound to port $tcpPort")
    }

    fun stop(awaitTermination: Boolean = true) {
        ioAcceptor.dispose(awaitTermination)
    }
}
