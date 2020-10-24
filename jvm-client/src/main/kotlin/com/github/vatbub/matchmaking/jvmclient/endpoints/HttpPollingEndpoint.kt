/*-
 * #%L
 * matchmaking.jvm-client
 * %%
 * Copyright (C) 2016 - 2020 Frederik Kammel
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
package com.github.vatbub.matchmaking.jvmclient.endpoints

import com.github.vatbub.matchmaking.common.InteractionConverter
import com.github.vatbub.matchmaking.common.Request
import com.github.vatbub.matchmaking.common.Response
import com.github.vatbub.matchmaking.common.data.Room
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.common.requests.GetRoomDataRequest
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.jvmclient.EndpointConfiguration
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class HttpPollingEndpoint(
        configuration: EndpointConfiguration.HttpPollingEndpointConfig,
        onExceptionHappened: (e: Throwable) -> Unit
) : ClientEndpoint<EndpointConfiguration.HttpPollingEndpointConfig>(configuration, onExceptionHappened) {
    private val httpClient = OkHttpClient()
    private var internalIsConnected = false
    override val isConnected: Boolean
        get() = internalIsConnected
    private val networkThreadPool by lazy { Executors.newCachedThreadPool() }
    private val subscriptionExecutorService by lazy { Executors.newSingleThreadScheduledExecutor() }

    override fun <T : Response> sendRequestImpl(request: Request, responseHandler: (T) -> Unit) {
        networkThreadPool.submit {
            val requestBody = InteractionConverter.serialize(request).toRequestBody()
            val httpRequest = okhttp3.Request.Builder()
                    .url(configuration.finalUrl)
                    .post(requestBody)
                    .build()
            httpClient.newCall(httpRequest).execute().use { response ->
                response.body?.use { body ->
                    val responseJson = body.string()
                    val deserializedResponse = InteractionConverter.deserialize<T>(responseJson)
                    verifyResponseIsNotAnException(deserializedResponse)
                    responseHandler(deserializedResponse)
                }
            }
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
