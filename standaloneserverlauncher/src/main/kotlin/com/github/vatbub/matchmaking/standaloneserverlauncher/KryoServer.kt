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
import com.esotericsoftware.kryonet.Server
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.registerClasses
import com.github.vatbub.matchmaking.common.responses.BadRequestException
import com.github.vatbub.matchmaking.common.responses.InternalServerErrorException
import com.github.vatbub.matchmaking.server.logic.IpAddressHelper
import com.github.vatbub.matchmaking.server.logic.ServerContext
import com.github.vatbub.matchmaking.server.logic.configuration.Configuration
import com.github.vatbub.matchmaking.server.logic.configuration.ConfigurationManager
import com.github.vatbub.matchmaking.server.logic.sockets.Session

class KryoServer(tcpPort: Int, udpPort: Int?, initialServerContext: ServerContext? = null) {
    val server = Server()
    private var serverContext: ServerContext
    private val sessions = mutableMapOf<Connection, KryoSessionWrapper>()

    init {
        serverContext = if (initialServerContext != null)
            initialServerContext
        else {
            ConfigurationManager.onChangeListeners.add(this::reloadConfiguration)
            ConfigurationManager.currentConfiguration.getAsServerContext()
        }
        serverContext.resetMessageHandlers()

        if (udpPort == null)
            server.bind(tcpPort)
        else
            server.bind(tcpPort, udpPort)

        server.kryo.registerClasses()
        server.addListener(KryoListener())
        server.start()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun reloadConfiguration(oldConfiguration: Configuration, newConfiguration: Configuration) {
        serverContext = newConfiguration.getAsServerContext()
        serverContext.resetMessageHandlers()
    }

    inner class KryoListener : Listener() {

        override fun connected(connection: Connection) {
            this@KryoServer.sessions[connection] = KryoSessionWrapper(connection)
        }

        override fun disconnected(connection: Connection?) {
            val session = this@KryoServer.sessions.remove(connection) ?: return
            this@KryoServer.serverContext.messageDispatcher.dispatchWebsocketSessionClosed(session)
        }

        override fun received(connection: Connection, receivedObject: Any) {
            val session = this@KryoServer.sessions[connection]
            println("[SERVER] Received object: $receivedObject") // TODO: Logging framework
            if (session == null) {
                val exceptionMessage = "Unknown connection object"
                val responseException = InternalServerErrorException(exceptionMessage)
                try {
                    connection.sendUDP(responseException)
                } catch (e: IllegalStateException) {
                    connection.sendTCP(responseException)
                }
                throw IllegalStateException(exceptionMessage)
            }
            if (receivedObject is FrameworkMessage.KeepAlive) return
            val response = handleReceivedObject(connection, session, receivedObject)
            session.sendObjectAsync(response)
        }

        private fun handleReceivedObject(connection: Connection, session: Session, receivedObject: Any): Response {
            if (receivedObject !is Request) return BadRequestException("Object class not recognized, object must be a subclass of com.github.vatbub.matchmaking.common.Request")
            val inetAddress = connection.remoteAddressTCP.address
            return this@KryoServer.serverContext.messageDispatcher.dispatchOrCreateException(
                    receivedObject,
                    IpAddressHelper.castToIpv4OrNull(inetAddress),
                    IpAddressHelper.castToIpv6OrNull(inetAddress),
                    session
            )
        }
    }
}
