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

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.common.responses.BadRequestException
import com.github.vatbub.matchmaking.common.responses.InternalServerErrorException
import com.github.vatbub.matchmaking.server.logic.IpAddressHelper
import com.github.vatbub.matchmaking.server.logic.ServerContext
import com.github.vatbub.matchmaking.server.logic.configuration.Configuration
import com.github.vatbub.matchmaking.server.logic.configuration.ConfigurationManager
import com.github.vatbub.matchmaking.server.logic.sockets.Session
import org.apache.mina.core.service.IoHandlerAdapter
import org.apache.mina.core.session.IdleStatus
import org.apache.mina.core.session.IoSession
import org.apache.mina.filter.codec.ProtocolCodecFilter
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory
import org.apache.mina.filter.logging.LoggingFilter
import org.apache.mina.transport.socket.nio.NioSocketAcceptor
import java.net.DatagramSocket
import java.net.InetSocketAddress

class KryoServer(internal val tcpPort: Int, internal val udpPort: Int?, initialServerContext: ServerContext? = null) {
    private val tcpIoAcceptor = NioSocketAcceptor()
    private val udpSocket = if (udpPort != null) DatagramSocket(udpPort) else null
    internal var serverContext: ServerContext
    private val sessions = mutableMapOf<IoSession, KryoSessionWrapper>()

    init {
        serverContext = if (initialServerContext != null)
            initialServerContext
        else {
            ConfigurationManager.onChangeListeners.add(this::reloadConfiguration)
            ConfigurationManager.currentConfiguration.getAsServerContext()
        }
        serverContext.resetMessageHandlers()
    }

    fun start() {
        // TODO: Specify the logging levels for each event type (see http://mina.apache.org/mina-project/userguide/ch12-logging-filter/ch12-logging-filter.html)
        tcpIoAcceptor.filterChain.addLast("logger", LoggingFilter())
        tcpIoAcceptor.filterChain.addLast("codec", ProtocolCodecFilter(ObjectSerializationCodecFactory()))
        tcpIoAcceptor.handler = RequestHandler()
        tcpIoAcceptor.sessionConfig.readBufferSize = 2048
        tcpIoAcceptor.sessionConfig.setIdleTime(IdleStatus.BOTH_IDLE, 10)

        tcpIoAcceptor.bind(InetSocketAddress(tcpPort))
        logger.info("MINA tcp server bound to port $tcpPort")
    }

    fun stop(awaitTermination: Boolean = false) {
        tcpIoAcceptor.dispose(awaitTermination)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun reloadConfiguration(oldConfiguration: Configuration, newConfiguration: Configuration) {
        logger.info("Loading new configuration into KryoServer...")
        serverContext = newConfiguration.getAsServerContext()
        serverContext.resetMessageHandlers()
    }

    inner class RequestHandler : IoHandlerAdapter() {
        override fun messageReceived(ioSession: IoSession?, message: Any?) {
            super.messageReceived(ioSession, message)
            requireNotNull(ioSession)
            val session = this@KryoServer.sessions[ioSession]
            logger.trace("Server: Received message: $message")
            if (session == null) {
                val exceptionMessage = "Unknown connection object"
                val responseException = InternalServerErrorException(exceptionMessage)
                ioSession.write(responseException)
                throw IllegalStateException(exceptionMessage)
            }
            if (message == null) {
                val exceptionMessage = "Message must not be null"
                val responseException = BadRequestException(exceptionMessage)
                ioSession.write(responseException)
                throw IllegalStateException(exceptionMessage)
            }
            if (message !is Request) {
                val exceptionMessage = "Illegal message class: ${message.javaClass.name}"
                val responseException = BadRequestException(exceptionMessage)
                ioSession.write(responseException)
                throw IllegalStateException(exceptionMessage)
            }
            val response = handleReceivedObject(ioSession, session, message)
            session.sendObjectAsync(response)
        }

        override fun sessionOpened(ioSession: IoSession?) {
            super.sessionOpened(ioSession)
            requireNotNull(ioSession)
            this@KryoServer.sessions[ioSession] = KryoSessionWrapper(ioSession)
        }

        override fun sessionClosed(ioSession: IoSession?) {
            super.sessionClosed(ioSession)
            val sessionWrapper = this@KryoServer.sessions.remove(ioSession) ?: return
            this@KryoServer.serverContext.messageDispatcher.dispatchWebsocketSessionClosed(sessionWrapper)
        }

        override fun exceptionCaught(ioSession: IoSession?, cause: Throwable) {
            super.exceptionCaught(ioSession, cause)
            requireNotNull(ioSession)
            logger.error("An internal server error occurred:", cause)
            val responseException = InternalServerErrorException(cause.javaClass.name + ", " + cause.message)
            ioSession.write(responseException)
        }

        private fun handleReceivedObject(ioSession: IoSession, session: Session, receivedObject: Request): Response {
            val remoteSocketAddress = requireNotNull(ioSession.remoteAddress)
            require(remoteSocketAddress is InetSocketAddress) { "The remote socket address is not a InetSocketAddress" }
            val inetAddress = requireNotNull(remoteSocketAddress.address)

            return this@KryoServer.serverContext.messageDispatcher.dispatchOrCreateException(
                    receivedObject,
                    IpAddressHelper.castToIpv4OrNull(inetAddress),
                    IpAddressHelper.castToIpv6OrNull(inetAddress),
                    session
            )
        }
    }
}
