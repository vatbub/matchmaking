package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.common.requests.GetRoomDataRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.jvmclient.EndpointConfiguration
import com.jsunsoft.http.HttpRequestBuilder
import org.apache.commons.io.IOUtils
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

internal class HttpPollingEndpoint(configuration: EndpointConfiguration.HttpPollingEndpointConfig) : ClientEndpoint<EndpointConfiguration.HttpPollingEndpointConfig>(configuration) {
    private var internalIsConnected = false
    override val isConnected: Boolean
        get() = internalIsConnected
    private val networkThreadPool by lazy { Executors.newCachedThreadPool() }
    private val subscriptionExecutorService by lazy { Executors.newSingleThreadScheduledExecutor() }

    override fun <T : Response> sendRequestImpl(request: Request, responseHandler: (T) -> Unit) {
        networkThreadPool.submit {
            val requestJson = InteractionConverter.serialize(request)
            val httpRequest = HttpRequestBuilder.createPost(configuration.finalUrl.toURI(), String::class.java)
                    .addDefaultHeader("Content-Type", "application/json; charset=UTF-8")
                    .responseDeserializer { responseContext ->
                        val charsetHeader = responseContext.httpResponse.getFirstHeader("charset")
                        var encoding: String? = null
                        if (charsetHeader != null) {
                            val charsetParts = charsetHeader.value.split(";")
                            charsetParts.forEach { charsetPart ->
                                if (charsetPart.startsWith("charset="))
                                    encoding = charsetPart.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
                            }
                        }

                        if (encoding == null)
                            encoding = "UTF-8"

                        val responseString = IOUtils.toString(responseContext.content, encoding)
                        responseString
                    }.build()
            val httpResponse = httpRequest.executeWithBody(requestJson)
            val responseJson = if (httpResponse.hasContent())
                httpResponse.get()
            else
                httpResponse.errorText

            responseHandler(InteractionConverter.deserialize(responseJson))
        }
    }

    override fun abortRequestsOfType(sampleRequest: Request) {
        // No-op as requests are handled immediately after receiving them
    }

    override fun subscribeToRoom(connectionId: String, password: String, roomId: String, newRoomDataHandler: (Room) -> Unit) {
        val command = Runnable {
            sendRequest<GetRoomDataResponse>(GetRoomDataRequest(connectionId, password, roomId)) {
                val roomCopy = it.room
                if (roomCopy == null)
                    logger.warn("Received a GetRoomDataResponse which did not include any room data!")
                else
                    newRoomDataHandler(roomCopy)
            }
        }
        subscriptionExecutorService.scheduleAtFixedRate(
                command,
                0,
                configuration.pollInterval.timeToSleepBetweenUpdatesInMilliSeconds.toLong(),
                TimeUnit.MILLISECONDS)
    }

    override fun connect() {
        internalIsConnected = true
    }

    override fun terminateConnection() {
        networkThreadPool.shutdown()
        subscriptionExecutorService.shutdown()
        internalIsConnected = false
    }

}