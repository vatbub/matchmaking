package com.github.vatbub.matchmaking.jvmclient

import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.responses.*

sealed class ClientEndpoint<T : EndpointConfiguration>(protected val configuration: T) {
    abstract fun <T : Response> sendRequest(request: Request, responseHandler: ((T) -> Unit))
    abstract fun abortRequestsOfType(sampleRequest: Request)
    abstract fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (Room) -> Unit)
    abstract fun terminateConnection()

    internal fun verifyResponseIsNotAnException(response: Response) {
        when (response) {
            is AuthorizationException -> throw response
            is BadRequestException -> throw response
            is InternalServerErrorException -> throw response
            is NotAllowedException -> throw response
            is UnknownConnectionIdException -> throw response
        }
    }

    class WebsocketEndpoint(configuration: EndpointConfiguration.WebsocketEndpointConfig) : ClientEndpoint<EndpointConfiguration.WebsocketEndpointConfig>(configuration) {
        override fun <T : Response> sendRequest(request: Request, responseHandler: (T) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun abortRequestsOfType(sampleRequest: Request) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (Room) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun terminateConnection() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    class HttpPollingEndpoint(configuration: EndpointConfiguration.HttpPollingEndpointConfig) : ClientEndpoint<EndpointConfiguration.HttpPollingEndpointConfig>(configuration) {
        override fun <T : Response> sendRequest(request: Request, responseHandler: (T) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun abortRequestsOfType(sampleRequest: Request) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (Room) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun terminateConnection() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    class KryoEndpoint(configuration: EndpointConfiguration.KryoEndpointConfiguration) : ClientEndpoint<EndpointConfiguration.KryoEndpointConfiguration>(configuration) {
        override fun <T : Response> sendRequest(request: Request, responseHandler: (T) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun abortRequestsOfType(sampleRequest: Request) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (Room) -> Unit) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun terminateConnection() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}