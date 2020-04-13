package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.github.vatbub.matchmaking.common.*
import com.github.vatbub.matchmaking.common.HttpStatusCodeErrorLevel.*
import com.github.vatbub.matchmaking.common.WebsocketCloseCode.*
import com.github.vatbub.matchmaking.jvmclient.EndpointConfiguration
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake

class WebsocketEndpoint(configuration: EndpointConfiguration.WebsocketEndpointConfig) : SocketClientEndpoint<EndpointConfiguration.WebsocketEndpointConfig>(configuration) {
    private var internalConnected = false

    private var client = object : WebSocketClient(configuration.finalUrl.toURI()) {
        override fun onOpen(handshakedata: ServerHandshake?) {
            internalConnected = true
            if (handshakedata == null) {
                logger.info { "Websocket connection established, handshake data was null" }
                return
            }

            val statusCode = HttpStatusCode.fromStatusCode(handshakedata.httpStatus.toInt())
            when (statusCode.errorLevel) {
                Information, Success -> logger.info(statusCode::generateLogMessage)
                Redirect -> logger.warn(statusCode::generateLogMessage)
                ClientError, ServerError, ProprietaryError -> logger.error(statusCode::generateLogMessage)
            }
        }

        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            internalConnected = false
            when (val closeCode = WebsocketCloseCode.fromCode(code)) {
                PROTOCOL_ERROR, CANNOT_ACCEPT, NOT_CONSISTENT, VIOLATED_POLICY, TOO_BIG, NO_EXTENSION -> {
                    logger.error { closeCode.generateLogMessage(reason ?: "<null>", remote, false) }
                    return
                }
                NO_STATUS_CODE, CLOSED_ABNORMALLY, UNEXPECTED_CONDITION, SERVICE_RESTART,
                TRY_AGAIN_LATER, TLS_HANDSHAKE_FAILURE ->
                    logger.error { closeCode.generateLogMessage(reason ?: "<null>", remote, true) }
                else ->
                    logger.info { closeCode.generateLogMessage(reason ?: "<null>", remote, false) }
            }
            reconnect()
        }

        override fun onMessage(message: String?) {
            synchronized(Lock) {
                if (message == null) {
                    logger.warn { "Websocket client received a message that was null." }
                    return
                }
                logger.trace { "Client: Received the following message: $message" }
                logger.debug { "Parsing the message..." }
                val response: Response = InteractionConverter.deserialize(message)
                logger.debug("Message parsed.")
                processResponse(response)
            }
        }

        override fun onError(ex: Exception?) {
            logger.error(ex) { "An exception occurred in a web socket." }
        }

    }

    override val isConnected: Boolean
        get() = internalConnected

    override fun socketSendRequestImpl(request: Request) {
            logger.debug { "Serializing th request..." }
            val serializedRequest = InteractionConverter.serialize(request)
            logger.debug { "Request serialized, sending..." }
            client.send(serializedRequest)
        }


    override fun connect() {
        super.connect()
        client.connect()
    }

    override fun terminateConnection() {
        super.terminateConnection()
        client.closeBlocking()
    }
}