package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.jvmclient.EndpointConfiguration

internal class WebsocketEndpoint(configuration: EndpointConfiguration.WebsocketEndpointConfig) : ClientEndpoint<EndpointConfiguration.WebsocketEndpointConfig>(configuration) {
    override val isConnected: Boolean
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override fun <T : Response> sendRequestImpl(request: Request, responseHandler: (T) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun abortRequestsOfType(sampleRequest: Request) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (Room) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun terminateConnection() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}