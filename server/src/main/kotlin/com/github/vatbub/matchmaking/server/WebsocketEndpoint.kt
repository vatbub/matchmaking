/*-
 * #%L
 * matchmaking.server
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
package com.github.vatbub.matchmaking.server

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.server.logic.IpAddressHelper
import com.github.vatbub.matchmaking.server.logic.ServerContext
import com.github.vatbub.matchmaking.server.logic.configuration.Configuration
import com.github.vatbub.matchmaking.server.logic.configuration.ConfigurationManager
import java.net.Inet4Address
import java.net.Inet6Address
import javax.servlet.http.HttpSession
import javax.websocket.*
import javax.websocket.server.HandshakeRequest
import javax.websocket.server.ServerEndpoint
import javax.websocket.server.ServerEndpointConfig

const val httpHeadersKey = "httpHeaders"

@ServerEndpoint("/websocket", configurator = WebSocketSessionConfigurator::class)
class WebsocketEndpoint(initialServerContext: ServerContext? = null) {
    private var session: Session? = null
    private var sessionWrapper: WebsocketSessionWrapper? = null
    private var endpointConfig: EndpointConfig? = null

    @Suppress("UNCHECKED_CAST")
    val httpHeaders: Map<String, List<String>>?
        get() {
            val endpointConfigCopy = endpointConfig ?: return null
            return endpointConfigCopy.userProperties[HttpSession::class.java.name] as Map<String, List<String>>
        }

    private val remoteIpString: String?
        get() {
            val forwardedForHeaderUppercase = getHeaderIfExists("X-FORWARDED-FOR")
            if (forwardedForHeaderUppercase != null)
                return forwardedForHeaderUppercase

            val forwardedForHeaderLowercase = getHeaderIfExists("x-forwarded-for")
            if (forwardedForHeaderLowercase != null)
                return forwardedForHeaderLowercase

            val originHeaderUppercase = getHeaderIfExists("ORIGIN")
            if (originHeaderUppercase != null)
                return originHeaderUppercase

            val originHeaderLowercase = getHeaderIfExists("origin")
            if (originHeaderLowercase != null)
                return originHeaderLowercase

            return null
        }

    private val remoteInet4Address: Inet4Address?
        get() = IpAddressHelper.convertToIpv4(remoteIpString)

    private val remoteInet6Address: Inet6Address?
        get() = IpAddressHelper.convertToIpv6(remoteIpString)

    private fun getHeaderIfExists(headerName: String): String? {
        val httpHeadersCopy = httpHeaders ?: return null
        if (!httpHeadersCopy.containsKey(headerName))
            return null
        val result = httpHeadersCopy[headerName] ?: return null
        if (result.isEmpty())
            return null
        return result[0]
    }

    private var serverContext: ServerContext

    init {
        serverContext = if (initialServerContext != null)
            initialServerContext
        else {
            ConfigurationManager.onChangeListeners.add(this::reloadConfiguration)
            ConfigurationManager.currentConfiguration.getAsServerContext()
        }
        serverContext.resetMessageHandlers()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun reloadConfiguration(oldConfiguration: Configuration, newConfiguration: Configuration) {
        serverContext = newConfiguration.getAsServerContext()
        serverContext.resetMessageHandlers()
    }

    @OnOpen
    fun open(session: Session, endpointConfig: EndpointConfig) {
        println("Session opened")
        this.session = session
        this.sessionWrapper = WebsocketSessionWrapper(session)
        this.endpointConfig = endpointConfig
    }

    @OnMessage
    fun onTextMessage(session: Session, message: String) {
        println("Received message:")
        println(message)
        session.asyncRemote.sendText(message)
        val request = InteractionConverter.deserializeRequest<Request>(message)
        val responseInteraction =
                serverContext.messageDispatcher.dispatchOrCreateException(
                        request,
                        remoteInet4Address,
                        remoteInet6Address,
                        sessionWrapper!!
                )
        val responseJson = InteractionConverter.serialize(responseInteraction)
        session.asyncRemote.sendText(responseJson)
    }

    @OnClose
    fun onSessionClose(session: Session, closeReason: CloseReason) {
        println("Session closed")
        val sessionWrapperCopy = sessionWrapper ?: return
        serverContext.messageDispatcher.dispatchWebsocketSessionClosed(sessionWrapperCopy)
    }
}

class WebSocketSessionConfigurator : ServerEndpointConfig.Configurator() {
    override fun modifyHandshake(
            serverEndpointConfig: ServerEndpointConfig?,
            request: HandshakeRequest?,
            response: HandshakeResponse?
    ) {
        if (request == null) return
        if (serverEndpointConfig == null) return

        serverEndpointConfig.userProperties[httpHeadersKey] = request.headers
    }
}
