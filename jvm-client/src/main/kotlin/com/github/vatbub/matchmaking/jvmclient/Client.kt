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
import com.github.vatbub.matchmaking.common.requests.Operation.*
import com.github.vatbub.matchmaking.common.responses.DisconnectResponse
import com.github.vatbub.matchmaking.common.responses.GetConnectionIdResponse
import com.github.vatbub.matchmaking.common.responses.GetRoomDataResponse
import com.github.vatbub.matchmaking.common.responses.JoinOrCreateRoomResponse
import com.github.vatbub.matchmaking.common.responses.Result.Nothing
import com.github.vatbub.matchmaking.jvmclient.endpoints.ClientEndpoint
import com.github.vatbub.matchmaking.jvmclient.endpoints.HttpPollingEndpoint
import com.github.vatbub.matchmaking.jvmclient.endpoints.KryoEndpoint
import com.github.vatbub.matchmaking.jvmclient.endpoints.WebsocketEndpoint
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

/**
 * Superclass for endpoint configurations
 */
sealed class EndpointConfiguration {
    /**
     * Configuration for a websocket connection to a server
     * @param hostUrl The URL of the host to connect to without the `websocket` part at the end.
     */
    data class WebsocketEndpointConfig(private val hostUrl: URL) : EndpointConfiguration() {
        private val suffix = "websocket"

        /**
         * The actual url of the websocket.
         * This url is constructed by appending `/websocket` to the end of the supplied host url.
         */
        val finalUrl = URL(hostUrl, suffix)
    }

    /**
     * Configuration for a http polling connection to a server
     * @param hostUrl The URL of the host to connect to without the `matchmaking` part at the end.
     */
    data class HttpPollingEndpointConfig(val hostUrl: URL, val pollInterval: PollInterval = PollInterval.Medium) : EndpointConfiguration() {
        private val suffix = "matchmaking"

        /**
         * The actual url of the server.
         * This url is constructed by appending `/matchmaking` to the end of the supplied host url.
         */
        val finalUrl = URL(hostUrl, suffix)
    }

    /**
     * Configuration for a kryo net connection to a server
     * @param host The host name or host ip of the server
     * @param tcpPort The tcp port to connect to (Optional).
     * @param udpPort The udp port to connect to. If no udp port is specified, udp will be disabled.
     * @param timeout Timeout to connect to the host
     */
    data class KryoEndpointConfiguration(val host: String, val tcpPort: Int = KryoCommon.defaultTcpPort, val udpPort: Int? = null, val timeout: Int = 5000) : EndpointConfiguration()
}

/**
 * Game client which connects to a server.
 * This is the class you should use to connect to your server.
 *
 * **IMPORTANT:** The constructor will already establish the connection to the server, but it will not request a connection id.
 * You therefore need to call [requestConnectionId] before you do anything else.
 *
 * **IMPORTANT:** Because one client typically only participates in one game room, this class is designed to only handle one game room per instance.
 * You therefore need multiple instances if you wish to connect to multiple rooms at once.
 *
 * @param configurations A list of [EndpointConfiguration]s which specify how the client can connect to your server. If you specify multiple configurations, the client will try the first one. If that fails, it will try the second one and so on
 * @param onConnectedUsersChange Callback which is called whenever the list of connected users changes.
 * @param onGameStateChange Callback which is called whenever [Room.gameState] changes.
 * @param onGameStarted Callback which is called when the game host starts the game.
 * @param onDataToBeSentToHostChange Callback which is called whenever any client adds any data to [Room.dataToBeSentToTheHost]
 */
class Client(
        configurations: List<EndpointConfiguration>,
        private val onConnectedUsersChange: (oldValue: List<User>?, newValue: List<User>) -> Unit,
        private val onGameStateChange: (oldValue: GameData?, newValue: GameData) -> Unit,
        private val onGameStarted: () -> Unit,
        private val onDataToBeSentToHostChange: (oldValue: List<GameData>?, newValue: List<GameData>) -> Unit,
        private val onExceptionHappened: (exception: Throwable) -> Unit
) {
    /**
     * The connection id for this client or `null` if not connected
     */
    var connectionId: String? = null
        private set

    /**
     * The password for this client or `null` if not connected
     */
    var password: String? = null
        private set
    private val safeConnectionId: String
        get() = connectionId
                ?: IllegalStateException("ConnectionId unknown, use requestConnectionId() to request a new connection id and password from the server").reportExceptionAndThrow()
    private val safePassword: String
        get() = password
                ?: IllegalStateException("Password unknown, use requestConnectionId() to request a new connection id and password from the server").reportExceptionAndThrow()
    private var currentRoomId: String? = null
    private val safeCurrentRoomId: String
        get() = currentRoomId
                ?: IllegalStateException("CurrentRoomId unknown, use joinRoom(...) or joinOrCreateRoom(...) to join a room").reportExceptionAndThrow()

    /**
     * The [Room] which this client is currently connected to or `null` if not connected or if no room was joined yet.
     */
    var currentRoom: Room? = null
        private set
    private val safeCurrentRoom: Room
        get() = currentRoom
                ?: IllegalStateException("CurrentRoom unknown, use joinRoom(...) or joinOrCreateRoom(...) to join a room").reportExceptionAndThrow()

    /**
     * `true` if connected to a server, `false` otherwise
     */
    val connected: Boolean
        get() = connectionId != null && password != null

    /**
     * `true` if a room was joined, `false` otherwise
     */
    val roomConnected: Boolean
        get() = currentRoomId != null

    private val endpoint: ClientEndpoint<out EndpointConfiguration>

    init {
        var tempEndpoint: ClientEndpoint<out EndpointConfiguration>? = null
        configurations.forEach {
            try {
                if (tempEndpoint != null) return@forEach
                tempEndpoint = when (it) {
                    is EndpointConfiguration.WebsocketEndpointConfig -> WebsocketEndpoint(it, onExceptionHappened)
                    is EndpointConfiguration.HttpPollingEndpointConfig -> HttpPollingEndpoint(it, onExceptionHappened)
                    is EndpointConfiguration.KryoEndpointConfiguration -> KryoEndpoint(it, onExceptionHappened)
                }
                tempEndpoint!!.connect()
                return@forEach
            } catch (e: Exception) {
                logger.error(e) { "Unable to connect using this configuration, trying the next configuration (if there's more specified)..." }
            }
        }
        endpoint = tempEndpoint
                ?: ConnectException("Unable to connect using any of the specified configurations. See the log for more details.").reportExceptionAndThrow()
    }

    private fun Throwable.reportExceptionAndThrow(): kotlin.Nothing {
        onExceptionHappened(this)
        throw this
    }

    /**
     * Requests a connection id from the server and stores it in [connectionId] and [password].
     */
    fun requestConnectionId(onConnectionIdReceived: (connectionId: String) -> Unit) {
        if (connected) return
        synchronized(this) {
            if (connected) return
            endpoint.sendRequest<GetConnectionIdResponse>(GetConnectionIdRequest()) {
                connectionId = it.connectionId
                password = it.password
                onConnectionIdReceived(it.connectionId!!)
            }
        }
    }

    /**
     * Joins the room with the specified criteria.
     * @param userName The user name of this player.
     * @param whitelist If specified, only the user names mentioned in this list are allowed to be in the room.
     * @param blacklist If specified, rooms are not considered where any of the mentioned user names has joined.
     * @param minRoomSize The minimal number of connected clients required to start a game.
     * @param maxRoomSize The maximum number of connected clients (including this client).
     * @param requestedRoomId Specify this parameter to join the specific room with this id.
     * @throws IllegalArgumentException If the room cannot be joined, e.g. because no room is found which matches the specified criteria or for any other reason.
     */
    fun joinRoom(userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2, requestedRoomId: String? = null, onRoomJoined: ((roomId: String) -> Unit)? = null) =
            joinOrCreateRoom(JoinRoom, userName, whitelist, blacklist, minRoomSize, maxRoomSize, requestedRoomId, onRoomJoined)

    /**
     * Creates a new room on the server with the given parameters.
     * @param userName The user name of this player.
     * @param whitelist If specified, only players with user names mentioned in this list are allowed to join this room. If unspecified, any user is allowed to join the room except for users mentioned in [blacklist].
     * @param blacklist If specified, players with user names mentioned in this list are not allowed to join this room.
     * @param minRoomSize The minimum number of players required to start a game.
     * @param maxRoomSize The maximum number of players in the room. The server will make sure that no more players join the room once [maxRoomSize] is reached.
     * @throws IllegalArgumentException If the room cannot be created for any reason.
     */
    fun createRoom(userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2, onRoomJoined: ((roomId: String) -> Unit)? = null) =
            joinOrCreateRoom(CreateRoom, userName, whitelist, blacklist, minRoomSize, maxRoomSize, null, onRoomJoined)

    /**
     * Tries to join a room which matches the given criteria. If no matching room can be found, a room with the specified parameters is created.
     * @see [joinRoom] and [createRoom] for more documentation.
     */
    fun joinOrCreateRoom(userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2, onRoomJoined: ((roomId: String) -> Unit)? = null) =
            joinOrCreateRoom(JoinOrCreateRoom, userName, whitelist, blacklist, minRoomSize, maxRoomSize, null, onRoomJoined)

    private fun joinOrCreateRoom(operation: Operation, userName: String, whitelist: List<String>? = null, blacklist: List<String>? = null, minRoomSize: Int = 1, maxRoomSize: Int = 2, requestedRoomId: String? = null, onRoomJoined: ((roomId: String) -> Unit)?) {
        if (roomConnected) return
        synchronized(this) {
            if (roomConnected) return
            if (requestedRoomId != null && operation != JoinRoom)
                IllegalArgumentException("To request a specific room id, operation must be set to Operation.JoinRoom.").reportExceptionAndThrow()
            endpoint.sendRequest<JoinOrCreateRoomResponse>(JoinOrCreateRoomRequest(safeConnectionId, safePassword, operation, userName, whitelist, blacklist, minRoomSize, maxRoomSize)) {
                when (it.result) {
                    Nothing -> when (operation) {
                        JoinRoom -> IllegalArgumentException("Unable to join the specified room. This is probably because no room with the specified id exists.")
                        CreateRoom -> IllegalArgumentException("Unable to create a new room for an unknown reason")
                        JoinOrCreateRoom -> IllegalArgumentException("Unable to join or create a new room for an unknown reason")
                    }.reportExceptionAndThrow()
                    else -> {
                        logger.debug { "Room ${it.result.toInfixStringPastTense()}: ${it.roomId}" }
                        currentRoomId = it.roomId
                                ?: IllegalArgumentException("Server sent an illegal response: roomId not specified").reportExceptionAndThrow()
                        endpoint.subscribeToRoom(safeConnectionId, safePassword, safeCurrentRoomId, this::newRoomDataHandler)
                        onRoomJoined?.invoke(safeCurrentRoomId)
                    }
                }
            }
        }
    }

    /**
     * Terminates the current connection gracefully.
     * No-op if not connected to a server.
     */
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

    /**
     * Sends the specified data to the host of the game.
     * Fore specifically, the specified data is added to [Room.dataToBeSentToTheHost].
     * [Room.dataToBeSentToTheHost] is distributed by the server to all other client sin the room, including the host.
     * The host can then use this data to update [Room.gameState].
     * Other clients can also use [Room.dataToBeSentToTheHost] to extrapolate the game state (to reduce lag).
     * However, the host is not obliged to update [Room.gameState].
     * In theory, all clients could just calculate the game state from [Room.dataToBeSentToTheHost].
     *
     * @param data The data to be sent to the host.
     */
    fun sendDataToHost(data: List<GameData>) {
        synchronized(this) {
            endpoint.sendRequest<GetRoomDataResponse>(SendDataToHostRequest(safeConnectionId, safePassword, safeCurrentRoomId, data)) { processGetGameDataResponse(it) }
        }
    }

    private fun verifyIsHost() {
        val currentRoomCopy = currentRoom
                ?: IllegalStateException("Room data not yet retrieved, please wait with calling this method until currentRoom != null").reportExceptionAndThrow()
        if (!currentRoomCopy.amITheHost)
            IllegalStateException("You are not the host of the room, only the host is allowed to perform this action!").reportExceptionAndThrow()
    }

    /**
     * Starts the game in the current room.
     * More specifically, [Room.gameStarted] is set to `true` and the corresponding event is sent to all clients.
     *
     * **IMPORTANT:** Only the host of a room may start the game.
     *
     * @see [Room.amITheHost]
     * @throws IllegalStateException If this client is not the host of the room or if the room data is not yet retrieved.
     */
    fun startGame() {
        synchronized(this) {
            verifyIsHost()
            endpoint.sendRequest<GetRoomDataResponse>(StartGameRequest(safeConnectionId, safePassword, safeCurrentRoomId)) { processGetGameDataResponse(it) }
        }
    }

    /**
     * Updates [Room.gameState].
     * This usually happens because clients have added data to [Room.dataToBeSentToTheHost] and the game state is updated accordingly.
     *
     * **IMPORTANT:** Only the host of a room may update the game state.
     *
     * @param newGameState The new game state
     * @param processedData The [GameData] packets which were used to create the new game state. These packets will automatically be removed from [Room.dataToBeSentToTheHost] by the server.
     *
     * @see [Room.amITheHost]
     * @throws IllegalStateException If this client is not the host of the room or if the room data is not yet retrieved.
     */
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
                ?: IllegalArgumentException("Room unknown to server, try reconnecting").reportExceptionAndThrow()
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
