package com.github.vatbub.matchmaking.standaloneserverlauncher

import com.esotericsoftware.kryonet.Connection
import com.esotericsoftware.kryonet.Listener
import com.esotericsoftware.kryonet.Server
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.responses.BadRequestException
import com.github.vatbub.matchmaking.server.logic.ServerContext
import com.github.vatbub.matchmaking.server.logic.configuration.Configuration
import com.github.vatbub.matchmaking.server.logic.configuration.ConfigurationManager
import com.github.vatbub.matchmaking.server.logic.sockets.Session
import java.net.Inet4Address
import java.net.Inet6Address

class KryoServer(tcpPort: Int, udpPort: Int?, initialServerContext: ServerContext? = null) {
    private val server = Server()
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

        server.addListener(KryoListener(this))
        server.start()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun reloadConfiguration(oldConfiguration: Configuration, newConfiguration: Configuration) {
        serverContext = newConfiguration.getAsServerContext()
        serverContext.resetMessageHandlers()
    }

    class KryoListener(private val server: KryoServer) : Listener() {

        override fun connected(connection: Connection) {
            server.sessions[connection] = KryoSessionWrapper(connection)
        }

        override fun disconnected(connection: Connection?) {
            val session = server.sessions.remove(connection)?:return
            server.serverContext.messageDispatcher.dispatchWebsocketSessionClosed(session)
        }

        override fun received(connection: Connection, receivedObject: Any) {
            val session = server.sessions[connection] ?: throw IllegalStateException("Unknown connection object")
            val response = handleReceivedObject(connection, session, receivedObject)
            session.sendObjectAsync(response)
        }

        private fun handleReceivedObject(connection: Connection, session: Session, receivedObject: Any): Response {
            if (receivedObject !is Request) return BadRequestException("Object class not recognized, object must be a subclass of com.github.vatbub.matchmaking.common.Request")
            val inetAddress = connection.remoteAddressTCP.address
            val inet4Address = if (inetAddress is Inet4Address) inetAddress else null
            val inet6Address = if (inetAddress is Inet6Address) inetAddress else null
            return server.serverContext.messageDispatcher.dispatchOrCreateException(
                    receivedObject,
                    inet4Address,
                    inet6Address,
                    session
            )
        }
    }
}