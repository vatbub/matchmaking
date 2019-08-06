/*-
 * #%L
 * matchmaking.jvm-client
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
package com.github.vatbub.matchmaking.jvmclient

import com.github.vatbub.matchmaking.common.KryoCommon
import com.github.vatbub.matchmaking.common.data.GameData
import com.github.vatbub.matchmaking.common.data.User
import com.github.vatbub.matchmaking.common.logger
import com.github.vatbub.matchmaking.common.requests.*
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.JoinOrCreateRoomResponse
import com.github.vatbub.matchmaking.common.responses.Result.*
import com.github.vatbub.matchmaking.common.responses.Result.Nothing
import java.net.ConnectException
import java.net.URL
import com.github.vatbub.matchmaking.common.data.Room as DataRoom

enum class PollInterval(val timeToSleepBetweenUpdatesInMilliSeconds: Int) {
    Fast(250),
    Medium(500),
    Slow(1000),
    VerySlow(5000),
    ExtremelySlow(10000)
}

sealed class EndpointConfiguration {
    data class WebsocketEndpointConfig(private val hostUrl: URL) : EndpointConfiguration() {
        private val suffix = "websocket"
        val finalUrl = URL(hostUrl, suffix)
    }

    data class HttpPollingEndpointConfig(private val hostUrl: URL, val pollInterval: PollInterval = PollInterval.Medium) : EndpointConfiguration() {
        private val suffix = "matchmaking"
        val finalUrl = URL(hostUrl, suffix)
    }

    data class KryoEndpointConfiguration(val host: String, val tcpPort: Int = KryoCommon.defaultTcpPort, val udpPort: Int? = null, val timeout: Int = 5000) : EndpointConfiguration()
}

class Client(
        configurations: List<EndpointConfiguration>,
        private val onConnectedUsersChange: (oldValue: List<User>?, newValue: List<User>) -> Unit,
        private val onGameStateChange: (oldValue: GameData?, newValue: GameData) -> Unit,
        private val onGameStarted: () -> Unit,
        private val onDataToBeSentToHostChange: (oldValue: List<GameData>?, newValue: List<GameData>) -> Unit
) {
    var connectionId: String? = null
        private set
    var password: String? = null
        private set
    private val safeConnectionId: String
        get() = connectionId
                ?: throw IllegalStateException("ConnectionId unknown, use requestConnectionId() to request a new connection id and password from the server")
    private val safePassword: String
        get() = password
                ?: throw IllegalStateException("Password unknown, use requestConnectionId() to request a new connection id and password from the server")
    private var currentRoomId: String? = null
    private val safeCurrentRoomId: String
        get() = currentRoomId
                ?: throw IllegalStateException("CurrentRoomId unknown, use joinRoom(...) or joinOrCreateRoom(...) to join a room")
    var currentRoom: Room? = null
        private set
    private val safeCurrentRoom: Room
        get() = currentRoom
                ?: throw IllegalStateException("CurrentRoom unknown, use joinRoom(...) or joinOrCreateRoom(...) to join a room")
    val connected: Boolean
        get() = connectionId != null && password != null
    val roomConnected: Boolean
        get() = currentRoomId != null

    private val endpoint: ClientEndpoint<out EndpointConfiguration>

    init {
        var tempEndpoint: ClientEndpoint<out EndpointConfiguration>? = null
        configurations.forEach {
            try {
                if (tempEndpoint != null) return@forEach
                tempEndpoint = when (it) {
                    is EndpointConfiguration.WebsocketEndpointConfig -> ClientEndpoint.WebsocketEndpoint(it)
                    is EndpointConfiguration.HttpPollingEndpointConfig -> ClientEndpoint.HttpPollingEndpoint(it)
                    is EndpointConfiguration.KryoEndpointConfiguration -> ClientEndpoint.KryoEndpoint(it)
                }
                tempEndpoint!!.connect()
                return@forEach
            } catch (e: Exception) {
                logger.error("Unable to connect using this configuration, trying the next configuration (if there's more specified)...", e)
            }
        }
        endpoint = tempEndpoint
                ?: throw ConnectException("Unable to connect using any of the specified configurations. See the log for more details.")
    }

    fun requestConnectionId() {
        if (connected) return
        synchronized(this) {
            if (connected) return
            endpoint.sendRequest<GetConnectionIdResponse>(GetConnectionIdRequest()) {
                connectionId = it.connectionId
                password = it.password
            }
        }
    }

    fun joinRoom(userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2) =
            joinOrCreateRoom(Operation.JoinRoom, userName, whitelist, blacklist, minRoomSize, maxRoomSize)

    fun createRoom(userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2) =
            joinOrCreateRoom(Operation.CreateRoom, userName, whitelist, blacklist, minRoomSize, maxRoomSize)

    fun joinOrCreateRoom(userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2) =
            joinOrCreateRoom(Operation.JoinOrCreateRoom, userName, whitelist, blacklist, minRoomSize, maxRoomSize)

    private fun joinOrCreateRoom(operation: Operation, userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2) {
        if (roomConnected) return
        synchronized(this) {
            if (roomConnected) return
            endpoint.sendRequest<JoinOrCreateRoomResponse>(JoinOrCreateRoomRequest(safeConnectionId, safePassword, operation, userName, whitelist, blacklist, minRoomSize, maxRoomSize)) {
                when (it.result) {
                    RoomCreated -> logger.debug("Room created: ${it.roomId}")
                    RoomJoined -> {
                        logger.debug("Room joined: ${it.roomId}")
                        currentRoomId = it.roomId
                                ?: throw IllegalArgumentException("Server sent an illegal response: roomId not specified")
                        endpoint.subscribeToRoom(safeConnectionId, safePassword, safeCurrentRoomId, this::newRoomDataHandler)
                    }
                    Nothing -> logger.debug("Result of JoinOrCreateRoomRequest: Nothing")
                }
            }
        }
    }

    fun disconnect() {
        if (!endpoint.isConnected) return
        synchronized(this) {
            if (!endpoint.isConnected) return

            if (!connected) {
                endpoint.terminateConnection()
                return
            }

            endpoint.sendRequest<DisconnectResponse>(DisconnectRequest(safeConnectionId, safePassword)) {
                endpoint.terminateConnection()
            }
        }
    }

    fun sendDataToHost(data: List<GameData>) {
        synchronized(this) {
            endpoint.sendRequest<GetRoomDataResponse>(SendDataToHostRequest(safeConnectionId, safePassword, safeCurrentRoomId, data)) { processGetGameDataResponse(it) }
        }
    }

    private fun verifyIsHost() {
        val currentRoomCopy = currentRoom
                ?: throw IllegalStateException("Room data not yet retrieved, please wait with calling this method until currentRoom != null")
        if (!currentRoomCopy.amITheHost)
            throw IllegalStateException("You are not the host of the room, only the host is allowed to perform this action!")
    }

    fun startGame() {
        synchronized(this) {
            verifyIsHost()
            endpoint.sendRequest<GetRoomDataResponse>(StartGameRequest(safeConnectionId, safePassword, safeCurrentRoomId)) { processGetGameDataResponse(it) }
        }
    }

    fun updateGameState(newGameState: GameData, processedData: List<GameData>) {
        verifyIsHost()
        if (safeCurrentRoom.gameStarted) return
        synchronized(this) {
            if (safeCurrentRoom.gameStarted) return
            endpoint.sendRequest<GetRoomDataResponse>(UpdateGameStateRequest(safeConnectionId, safePassword, safeCurrentRoomId, newGameState, processedData)) { processGetGameDataResponse(it) }
        }
    }

    private fun processGetGameDataResponse(getGameDataResponse: GetRoomDataResponse) {
        val room = getGameDataResponse.room
                ?: throw IllegalArgumentException("Room unknown to server, try reconnecting")
        newRoomDataHandler(room)
    }

    private fun newRoomDataHandler(room: DataRoom) {
        val oldRoom = currentRoom
        currentRoom = Room(safeConnectionId, room)

        if (oldRoom == null || !oldRoom.connectedUsers.toTypedArray().contentEquals(room.connectedUsers.toTypedArray()))
            onConnectedUsersChange(oldRoom?.connectedUsers, room.connectedUsers)
        if (oldRoom == null || oldRoom.gameState != room.gameState)
            onGameStateChange(oldRoom?.gameState, room.gameState)
        if (oldRoom == null || (!oldRoom.gameStarted && room.gameStarted))
            onGameStarted.invoke()
        if (oldRoom == null || !oldRoom.dataToBeSentToTheHost.toTypedArray().contentEquals(room.dataToBeSentToTheHost.toTypedArray()))
            onDataToBeSentToHostChange(oldRoom?.dataToBeSentToTheHost, room.dataToBeSentToTheHost)
    }
}
